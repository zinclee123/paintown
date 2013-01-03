#!/usr/bin/env python

import sys
sys.path.append('../parser')
import peg, state

# This script reads a specification of a datastructure with fields in it and writes
# out a class that contains those fields and a way to serialize/deserialize them
# to a stream. This is similar to google's protobuf but using a much simpler
# implementation.

grammar = """
start-symbol: start
module: state
code: {{
import state    
}}
rules:
    start = includes:include* name:namespace (!"\n" .)* newline s* obj:struct {{ value = state.Program(includes, name, obj) }}
    include = "include" s+ what:include-string (!"\n" .)* s+ "\n"* {{ value = what }}
    include-string = string
                   | "<" contents:(!">" .)* ">" {{ value = "<%s>" % ''.join(contents) }}
    string = <quote> contents:(!<quote> !"\n" .)* <quote> {{ value = '"' + ''.join(contents) + '"' }}
    namespace = "namespace" s+ id:identifier
    struct = "struct" s* name:identifier s* "{" fields:(s* field)* s* "}" {{
        value = state.State(name)
for field in fields:
    value.addField(field)
    }}
    identifier = a:letter rest:letter_digit* {{ value = a + ''.join(rest) }}
    letter = [abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_]
    digit = [0123456789]
    number = all:digit+ {{ value = ''.join(all) }}
    letter_digit = letter
                  | digit
    array = "[" s* out:number s* "]" {{ value = out }}
    field = obj:struct s* name:identifier array:array? ";"? {{
    if array != None:
    value = state.Field(obj, name, array)
else:
    value = state.Field(obj, name)
        }}
          | type:type-identifier s+ name:identifier ";"? {{ value = state.Field(type, name) }}
    # Hack to use unsigned here because we don't want to specialize for c/c++
    type-modifier = "unsigned"
                  | "signed"
    # Handles c++ style namespaces
    type-identifier = modifier:type-modifier? s* name:identifier template:template? namespace:("::" type-identifier)? {{ value = state.makeType(modifier, name, template, namespace) }}
    template = "<" s* arg1:type-identifier args:(s* "," s* type-identifier)* s* ">" {{ value = [arg1] + args }}
    newline = "\n"
    s = " "
      | "\t"
      | "\n"
      | "\r"
      | comment
    comment = "//" (!"\n" .)* "\n"
            | "/*" inner-comment* "*/"
    inner-comment = comment
                  | !"*/" .
"""

def create_peg(grammar, kind = 'file'):
    # Get the parser for BNF
    peg_parser = peg.create_peg(peg.peg_bnf('peg'), 'string')
    # Then use it to parse the grammar and return a new peg
    return peg.create_peg(peg_parser(grammar), kind)

def generate_cpp(object, name = None, array = None):
    def make_init_field(field):
        if field.type_.isPOD() and not field.isArray():
            return '%(name)s = %(zero)s;' % {'name': field.name,
                                             'zero': field.zero()}
        return None

    def make_definition(field):
        if isinstance(field.type_, state.State):
            return generate_cpp(field.type_, field.name, field.array)
        array = ''
        if field.isArray():
            array = field.array
        return '%(type)s %(name)s%(array)s;' % {'name': field.name,
                                                'type': field.type_,
                                                'array': array}

    def indent(lines, much):
        if len(lines) == 0:
            return ''
        tabs = '    ' * much
        out = []
        for line in lines:
            out.extend(line.split('\n'))
        return '\n'.join([out[0]] + [tabs + line for line in out[1:]])

    inits = []
    for field in object.fields:
        maybe_init = make_init_field(field)
        if maybe_init != None:
            inits.append(maybe_init)
    definitions = [make_definition(field) for field in object.fields]
    instance = "";
    if name != None:
        instance = " %s" % name;
    arrayUse = ''
    if array != None:
        arrayUse = '[%s]' % array
    data = """
struct %(name)s{
    %(name)s(){
        %(init-fields)s
    }

    %(definitions)s
}%(maybe-instance)s%(array)s;
""" % {'name': object.name,
       'maybe-instance': instance,
       'init-fields': indent(inits, 2),
       'definitions': indent(definitions, 1),
       'array': arrayUse
      }
    return data

def md5(what):
    import hashlib
    m = hashlib.md5()
    m.update(what)
    return m.hexdigest()

def generate_program_cpp(program):
    header = "_serialize_%s_%s" % (program.namespace, md5(generate_cpp(program.struct)))
    includes = '\n'.join(['#include %s' % x for x in program.includes])
    data = """
#ifndef %s
#define %s

%s

namespace %s{
%s
}

#endif
""" % (header, header, includes, program.namespace, generate_cpp(program.struct))
    return data

def test1():
    parser = create_peg(grammar, 'string')

    test = """foo{
  int tomato
  bool frog
}
"""
    print generate_cpp(parser(test))

if len(sys.argv) < 2:
    print "Give an argument"
    sys.exit(0)

print generate_program_cpp(create_peg(grammar)(sys.argv[1]))
