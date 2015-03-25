#!/usr/bin/env python2.7
import json
import os
import re
import sys
from collections import OrderedDict

def rel(p):
    return os.path.join(os.path.dirname(__file__), p)

class Method(object):
    def __init__(self, d):
        self.name = d['method']
        self.classname = ''.join([x[0].upper() + x[1:] for x in self.name.split('.')])
        self.args = d['args']
        self.fields = d['fields']
        self.root, self.sub = self.name.split('.')

def get_methods(path):
    with open(path, 'r') as f:
        json_str = f.read()
        methods = json.JSONDecoder(object_pairs_hook=OrderedDict).decode(json_str)
        return [Method(m) for m in methods]

def write_response_codec(method):
    if not method.fields:
        return
    with open(rel('../src/main/scala/com/nglarry/slacka/codecs/responses/%s.scala' % method.classname), 'w') as f:
        fields_str = '  ' + ',\n  '.join('%s: %s' % (a['name'], a['type']) for a in method.fields)
        f.write('package com.nglarry.slacka.codecs.responses\n\n')
        f.write('import org.json4s._\n')
        f.write('import com.nglarry.slacka.codecs.types._\n\n')
        f.write('case class %s(\n' % method.classname)
        f.write(fields_str)
        f.write('\n)\n')

def write_api_fns(methods):
    grouped = group_methods(methods)
    lines = ['  //{{{']
    for root in grouped:
        lines.append('  object %s {' % root)
        for method in grouped[root]:
            classname = method.classname if method.fields else 'Empty'
            if not method.args:
                args_str = ''
                map_str = 'Map.empty'
            else:
                arg_strs = []
                for a in method.args:
                    if a['required']:
                        arg_strs.append('%s: %s' % (a['name'], a['type']))
                    else:
                        arg_strs.append('%s: Option[%s] = None' % (a['name'], a['type']))
                args_str = ', '.join(arg_strs) + ', '
                map_str = 'Map(%s)' % ', '.join('"%s" -> %s' % (a['name'], a['name']) for a in method.args)
            lines.append('    def %s(%shandler: SlackResponseHandler[%s] = defaultHandler) =' %
                         (method.sub, args_str, classname))
            lines.append('      request[%s]("%s", %s, handler = handler)' %
                         (classname, method.name, map_str))
        lines.append('  }')
        lines.append('')
    lines.append('  //}}}')
    o = '\n'.join(lines)
    api_path = rel('../src/main/scala/com/nglarry/slacka/api/SlackWebApi.scala')
    with open(api_path, 'r') as f:
        original = f.read()
    with open(api_path, 'w') as f:
        f.write(re.sub('  //{{{.+  //}}}', o, original, flags=re.M|re.S))


def group_methods(methods):
    d = OrderedDict()
    for m in methods:
        d.setdefault(m.root, []).append(m)
    return d

# object api {
#   def test(error: X, foo: X, handler: SlackResponseHandler[ApiTest] = defaultHandler) =
#     request[ApiTest]("api.test", Map("error" -> error, "foo" -> foo), handler = handler)
# }

def run():
    methods = get_methods(sys.argv[1])
    for method in methods:
        write_response_codec(method)
    write_api_fns(methods)

run()
