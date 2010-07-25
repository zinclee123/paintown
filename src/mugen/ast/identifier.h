#ifndef _paintown_ast_identifier_h_
#define _paintown_ast_identifier_h_

#include <list>
#include <string>
#include <sstream>
#include <algorithm>
#include <map>

#include "Value.h"
#include "util/funcs.h"

namespace Ast{

class Identifier: public Value {
public:
    Identifier(int line, int column, const std::list<std::string> & names):
    names(names),
    line(line),
    column(column){
    }

    Identifier(const std::list<std::string> & names):
    names(names),
    line(-1),
    column(-1){
    }

    static int lowerCase( int c ){
        return tolower(c);
    }
    
    virtual Element * copy() const {
        return new Identifier(line, column, names);
    }

    static std::string downcase(std::string str){
        std::transform(str.begin(), str.end(), str.begin(), lowerCase);
        return str;
    }
    
    virtual void walk(Walker & walker) const {
        walker.onIdentifier(*this);
    }

    bool operator==(const std::string & str) const {
        return downcase(toString()) == downcase(str);
    }
    
    virtual std::string getType() const {
        return "identifier";
    }

    virtual int getLine() const {
        return line;
    }

    virtual int getColumn() const {
        return column;
    }

    virtual std::string toString() const {
        std::ostringstream out;
        bool first = true;
        for (std::list<std::string>::const_iterator it = names.begin(); it != names.end(); it++){
            if (!first){
                out << ".";
            } else {
                first = false;
            }
            out << *it;
        }
        return Util::trim(out.str());
    }
    
    /*
    virtual bool referenced(const void * value) const {
        if (value == this){
            return true;
        }
        
        for (std::list<std::string*>::const_iterator it = names.begin(); it != names.end(); it++){
            if (value == *it){
                return true;
            }
        }

        return false;
    }
    */
    
    virtual void mark(std::map<const void*, bool> & marks) const {
        marks[this] = true;
    }

    virtual ~Identifier(){
    }

protected:
    std::list<std::string> names;
    int line, column;
};

static std::list<std::string> toList(const std::string & s){
    std::list<std::string> out;
    out.push_back(s);
    return out;
}

class SimpleIdentifier: public Identifier {
public:
    SimpleIdentifier(const std::string & name):
    Identifier(-1, -1, toList(name)){
    }
};

}

#endif
