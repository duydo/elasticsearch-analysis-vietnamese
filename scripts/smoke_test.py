#!/usr/bin/env python3
"""
End-to-end smoke test of the plugin through the Elasticsearch REST API.

Usage: smoke_test.py [URL]   (default http://localhost:9200; set ES_AUTH=user:password if security is enabled)

The node must have the plugin installed and the file config/analysis/vi_stop.txt containing the lines
"thông tin" and "việt nam" (used by the stopwords_path check).
"""
import base64
import concurrent.futures
import json
import os
import sys
import urllib.error
import urllib.request

URL = (sys.argv[1] if len(sys.argv) > 1 else "http://localhost:9200").rstrip("/")
HEADERS = {"Content-Type": "application/json"}
if os.environ.get("ES_AUTH"):
    HEADERS["Authorization"] = "Basic " + base64.b64encode(os.environ["ES_AUTH"].encode()).decode()

failures = []


def call(method, path, body=None, content_type=None):
    data = body if isinstance(body, (bytes, type(None))) else json.dumps(body).encode()
    headers = dict(HEADERS, **({"Content-Type": content_type} if content_type else {}))
    try:
        with urllib.request.urlopen(urllib.request.Request(URL + path, data, headers, method=method)) as r:
            return r.status, json.load(r)
    except urllib.error.HTTPError as e:
        return e.code, json.load(e)


def tokens(body, index=""):
    status, resp = call("POST", f"/{index}/_analyze" if index else "/_analyze", body)
    return [t["token"] for t in resp["tokens"]] if status == 200 else resp


def check(name, actual, expected):
    ok = actual == expected
    print(f"{'PASS' if ok else 'FAIL'} {name}" + ("" if ok else f"\n     expected {expected}\n     actual   {actual}"))
    if not ok:
        failures.append(name)


def create_index(name, body):
    call("DELETE", f"/{name}")
    status, resp = call("PUT", f"/{name}", body)
    assert status == 200, resp


# Built-in components
check("vi_analyzer", tokens({"analyzer": "vi_analyzer", "text": "Cộng hòa Xã hội chủ nghĩa Việt Nam"}),
      ["cộng hòa", "xã hội", "chủ nghĩa", "việt nam"])
check("vi_analyzer removes stop words", tokens({"analyzer": "vi_analyzer", "text": "Công nghệ thông tin của Việt Nam"}),
      ["công nghệ", "thông tin", "việt nam"])
check("vi_tokenizer", tokens({"tokenizer": "vi_tokenizer", "text": "Công nghệ thông tin của Việt Nam"}),
      ["Công nghệ", "thông tin", "của", "Việt Nam"])
status, resp = call("POST", "/_analyze", {"tokenizer": {"type": "vi_tokenizer", "keep_punctuation": True},
                                          "text": "Giá 3,5 triệu!"})
check("token types", [(t["token"], t["type"]) for t in resp["tokens"]],
      [("Giá", "<WORD>"), ("3.5", "<NUMBER>"), ("triệu", "<WORD>"), ("!", "<PUNCT>")])
check("split_url", tokens({"tokenizer": {"type": "vi_tokenizer", "split_url": True}, "text": "duydo.me"}),
      ["duy", "do", "me"])
check("split_host", tokens({"tokenizer": {"type": "vi_tokenizer", "split_host": True}, "text": "vnexpress.net"}),
      ["vnexpress", "net"])
status, resp = call("POST", "/_analyze", {"tokenizer": "vi_tokenizer", "char_filter": ["html_strip"],
                                          "filter": ["vi_stop"], "text": "<b>Việt Nam</b> của tôi"})
check("html_strip offsets", [(t["token"], t["start_offset"], t["end_offset"]) for t in resp["tokens"]],
      [("Việt Nam", 3, 15), ("tôi", 20, 23)])

# Stop word settings
create_index("smoke-stopwords", {"settings": {"analysis": {
    "filter": {"named": {"type": "vi_stop", "stopwords": "_vi_"},
               "ignore_case": {"type": "vi_stop", "ignore_case": True}},
    "analyzer": {"vi": {"type": "vi_analyzer", "stopwords": "_vietnamese_"},
                 "none": {"type": "vi_analyzer", "stopwords": "_none_"},
                 "custom": {"type": "vi_analyzer", "stopwords": ["thông tin"]},
                 "file": {"type": "vi_analyzer", "stopwords_path": "analysis/vi_stop.txt"},
                 "named_filter": {"tokenizer": "vi_tokenizer", "filter": ["lowercase", "named"]},
                 "ignore_case_filter": {"tokenizer": "vi_tokenizer", "filter": ["ignore_case"]}}}}})
text = "Công nghệ thông tin của Việt Nam"
for analyzer, expected in [("vi", ["công nghệ", "thông tin", "việt nam"]),
                           ("none", ["công nghệ", "thông tin", "của", "việt nam"]),
                           ("custom", ["công nghệ", "của", "việt nam"]),
                           ("file", ["công nghệ", "của"]),
                           ("named_filter", ["công nghệ", "thông tin", "việt nam"])]:
    check(f"stopwords: {analyzer}", tokens({"analyzer": analyzer, "text": text}, "smoke-stopwords"), expected)
check("stopwords: ignore_case", tokens({"analyzer": "ignore_case_filter", "text": "Của Việt Nam"}, "smoke-stopwords"),
      ["Việt Nam"])

# Invalid settings are rejected as bad requests
for name, analysis in [
    ("conflicting dict_path", {"tokenizer": {"t": {"type": "vi_tokenizer", "dict_path": "/other"}},
                               "analyzer": {"a": {"tokenizer": "t"}}}),
    ("stopwords_path outside config", {"analyzer": {"a": {"type": "vi_analyzer", "stopwords_path": "/etc/passwd"}}}),
    ("missing stopwords_path", {"analyzer": {"a": {"type": "vi_analyzer", "stopwords_path": "analysis/missing.txt"}}}),
]:
    call("DELETE", "/smoke-invalid")
    status, resp = call("PUT", "/smoke-invalid", {"settings": {"analysis": analysis}})
    check(f"rejects {name}", (status, resp.get("error", {}).get("type")), (400, "illegal_argument_exception"))

# Indexing, search and highlighting
create_index("smoke-search", {
    "settings": {"analysis": {"analyzer": {"folding": {"tokenizer": "vi_tokenizer",
                                                       "filter": ["lowercase", "vi_stop", "keep_folding"]}},
                              "filter": {"keep_folding": {"type": "asciifolding", "preserve_original": True}}}},
    "mappings": {"properties": {"body": {"type": "text", "analyzer": "folding"}}}})
call("POST", "/smoke-search/_doc/1?refresh=true", {"body": "Học tiếng Việt"})
status, resp = call("POST", "/smoke-search/_search", {"query": {"match": {"body": "tieng viet"}},
                                                      "highlight": {"fields": {"body": {}}}})
check("search without diacritics", [h["highlight"]["body"] for h in resp["hits"]["hits"]], [["Học <em>tiếng Việt</em>"]])

# Concurrent indexing: every document must be analyzed exactly like _analyze does it
texts = ["Công nghệ thông tin của Việt Nam phát triển mạnh", "Hà Nội là thủ đô 😀 của nước ta",
         "Giá vàng hôm nay tăng 3,5 triệu đồng"]
create_index("smoke-concurrency", {"settings": {"number_of_shards": 4, "number_of_replicas": 0},
                                   "mappings": {"properties": {"body": {"type": "text", "analyzer": "vi_analyzer",
                                                                        "term_vector": "with_positions_offsets"}}}})


def bulk(batch):
    lines = []
    for i in range(batch * 250, batch * 250 + 250):
        lines += [json.dumps({"index": {"_id": str(i)}}), json.dumps({"body": texts[i % 3]}, ensure_ascii=False)]
    return call("POST", "/smoke-concurrency/_bulk", ("\n".join(lines) + "\n").encode(), "application/x-ndjson")[1]["errors"]


with concurrent.futures.ThreadPoolExecutor(8) as executor:
    bulk_errors = any(executor.map(bulk, range(8)))
call("POST", "/smoke-concurrency/_refresh")


def analyzed(terms):
    return sorted((term, t["position"], t["start_offset"], t["end_offset"]) for term, v in terms for t in v["tokens"])


expected = []
for t in texts:
    resp = call("POST", "/smoke-concurrency/_analyze", {"field": "body", "text": t})[1]
    expected.append(sorted((x["token"], x["position"], x["start_offset"], x["end_offset"]) for x in resp["tokens"]))
mismatches = sum(
    analyzed(call("GET", f"/smoke-concurrency/_termvectors/{i}?fields=body&positions=true&offsets=true")[1]
             ["term_vectors"]["body"]["terms"].items()) != expected[i % 3]
    for i in range(2000))
check("concurrent indexing", (bulk_errors, mismatches), (False, 0))

print(f"\n{len(failures)} failure(s)" if failures else "\nAll checks passed")
sys.exit(1 if failures else 0)
