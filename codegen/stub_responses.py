import re
import urllib2
import HTMLParser
import json
import time
import traceback
from collections import OrderedDict

TYPE_STR = {
    int: 'Long',
    long: 'Long',
    bool: 'Boolean',
    str: 'String',
    unicode: 'String',
    float: 'Float',
    list: 'List[X]',
    dict: 'X',
    OrderedDict: 'X'
}

def fetch_url(url):
    response = urllib2.urlopen(url)
    return response.read()


def fetch_methods():
    body = fetch_url('https://api.slack.com/methods')
    pattern = re.compile(r'''href="/methods/(\w+\.\w+)"''')
    return pattern.findall(body)

def group_methods(methods):
    d = OrderedDict()
    for method in methods:
        a, b = method.split('.')
        d.setdefault(a, []).append(b)
    return OrderedDict(d)

def extract_arguments(body):
    p1 = r'''<table class="arguments full_width">(.+?)<\/table>'''
    table = re.findall(p1, body, flags=re.M|re.S)[0]
    p2 = r'''<tr>(.+?)<\/tr>'''
    rows = re.findall(p2, table, flags=re.M|re.S)
    res = []
    for row in rows[2:]:
        cols = re.findall(r'''(<td\b.+?\/td>)''', row, flags=re.M|re.S)
        arg = re.findall(r'''<code>(.+?)<\/code>''', cols[0], flags=re.M|re.S)[0]
        required = 'Required' in cols[2]
        res.append((arg, required))
    return res

def extract_fields(body):
    raw = re.findall(r'''<pre><code>(.+?)</code></pre>''', body, flags=re.M|re.S|re.U)[0]
    s = raw.decode('utf-8').encode('ascii', errors='ignore')
    unescaped = HTMLParser.HTMLParser().unescape(s)
    lines = []
    for line in re.sub(r'\.\.+,?', '', unescaped).splitlines():
        if line.strip() and line[-1] not in ('{', ',', '['):
            line = line + ','
        lines.append(line)
    processed = '\n'.join(lines)
    processed = re.sub(r''',\s+}''', '}', processed, flags=re.M|re.S)
    processed = re.sub(r''',\s+\]''', ']', processed, flags=re.M|re.S)
    processed = processed[:-1]
    response = json.JSONDecoder(object_pairs_hook=OrderedDict).decode(processed)
    return [(k, TYPE_STR[type(t)]) for k, t in response.items() if k != 'ok']

def fetch_method_info(method):
    body = fetch_url('https://api.slack.com/methods/%s' % method)
    args = extract_arguments(body)
    fields = extract_fields(body)
    return args, fields

def run():
    methods = fetch_methods()
    grouped = group_methods(methods)
    data = []
    for a in grouped:
        for b in grouped[a]:
            method = '%s.%s' % (a, b)
            print 'processing %s' % method
            try:
                args, fields = fetch_method_info(method)
                method_data = OrderedDict()
                method_data['method'] = method
                method_data['args'] = [OrderedDict([('name', arg), ('type', 'String'), ('required', req)]) for arg, req in args]
                method_data['fields'] = [OrderedDict([('name', name), ('type', type_)]) for name, type_ in fields]
                data.append(method_data)
            except Exception, e:
                print ' ! failed to process %s' % method
                print traceback.format_exc()
                continue
            time.sleep(0.1)
    with open('methods.json', 'w') as f:
        f.write(json.dumps(data, indent=2, separators=(',', ': ')))
