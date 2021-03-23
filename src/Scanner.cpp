/*
 * @Description: C语言词法分析器
 * @Author: xuzf
 * @Date: 2021-03-21 21:28:29
 * @FilePath: \BIT-MiniCC\src\Scanner.cpp
 */

#include <fstream>
#include <iostream>
#include <string>
#include <unordered_set>
#include <vector>

using namespace std;

enum DFA_STATE
{
    DFA_STATE_INITIAL,
    DFA_STATE_ID_0,

    DFA_STATE_INT_D_0,
    DFA_STATE_INT_OH,
    DFA_STATE_INT_O_0,
    DFA_STATE_INT_H_0,

    DFA_STATE_INT_SUFFIX_INITIAL,
    DFA_STATE_INT_SUFFIX_1_1,
    DFA_STATE_INT_SUFFIX_1_2,
    DFA_STATE_INT_SUFFIX_1_3,
    DFA_STATE_INT_SUFFIX_2_1,
    DFA_STATE_INT_SUFFIX_2_2,
    DFA_STATE_INT_SUFFIX_2_3,

    DFA_STATE_FLOAT_D_0,
    DFA_STATE_FLOAT_D_1,
    DFA_STATE_FLOAT_D_2,
    DFA_STATE_FLOAT_H_0,
    DFA_STATE_FLOAT_H_1,
    DFA_STATE_FLOAT_H_2,

    DFA_STATE_CHAR_STR_PREFIX,
    DFA_STATE_CHAR,

    DFA_STATE_STR,

    DFA_STATE_ADD,
    DFA_STATE_MINUS,
    DFA_STATE_AND,
    DFA_STATE_MUT,
    DFA_STATE_SIGN,
    DFA_STATE_DIVI,
    DFA_STATE_PERCENT_0,
    DFA_STATE_PERCENT_1,
    DFA_STATE_PERCENT_2,
    DFA_STATE_LESS_0,
    DFA_STATE_LESS_1,
    DFA_STATE_GREATER_0,
    DFA_STATE_GREATER_1,
    DFA_STATE_EQUAL,
    DFA_STATE_POWER,
    DFA_STATE_OR,
    DFA_STATE_COLON,
    DFA_STATE_HASHTAG,

};

class Scanner
{
private:
    unordered_set<string> keywordSet;
    unordered_set<char> nonPrefixOperator;
    vector<string> srcLines;
    int row, col;
    int iTknNum;

    void readFile(string &inputFilePath);
    static void writeFile(const string &strTokens, const string &inputFilePath);
    char getNextChar();
    static string ch2String(char ch);
    static bool isDigit(const char &ch);
    static bool isHexDigit(const char &ch);
    static bool isAlpha(const char &ch);
    static bool isIntSuffix(const char &ch);
    bool isAlphaOrDigit(const char &ch) const;
    string genToken(const string &lexme, const string &type);
    string genToken2(const string &lexme, const string &type);
    string genToken(const string &lexme, const string &type, int col, int row);

public:
    Scanner();
    ~Scanner();
    void run(string &inputFilePath);
};

Scanner::Scanner()
{
    // 初始化当前处理元素的行列坐标
    row = 0, col = 0;
    // 初始化token个数
    iTknNum = 0;
    // 定义关键词集合
    keywordSet = {
        "auto",
        "break",
        "case",
        "char",
        "const",
        "continue",
        "default",
        "do",
        "double",
        "else",
        "enum",
        "extern",
        "float",
        "for",
        "goto",
        "if",
        "inline",
        "int",
        "long",
        "register",
        "restrict",
        "return",
        "short",
        "signed",
        "sizeof",
        "static",
        "struct",
        "switch",
        "typedef",
        "union",
        "unsigned",
        "void",
        "volatile",
        "while",
    };
    nonPrefixOperator = {'[', ']', '(', ')', '{', '}', '.', '~', '?', ';', ','};
}

Scanner::~Scanner() = default;

void Scanner::readFile(string &inputFilePath)
{
    // 读入待处理的数据
    ifstream fin(inputFilePath, ios::in);
    if (!fin)
    {
        cout << "Error opening " << inputFilePath << " for input" << endl;
        exit(-1);
    }
    string buff;
    while (getline(fin, buff))
    {
        this->srcLines.emplace_back(buff);
    }
    fin.close();
}

void Scanner::writeFile(const string &strTokens, const string &inputFilePath)
{
    // 打开输出文件
    size_t pos = inputFilePath.find_last_of('.');
    string outputFilePath = inputFilePath.substr(0, pos) + ".tokens";
    fstream fout(outputFilePath, ios::out | ios::trunc);
    if (!fout)
    {
        cout << "Error opening " << outputFilePath << " for output" << endl;
        exit(-1);
    }
    fout << strTokens;
    fout.close();
}

char Scanner::getNextChar()
{
    char ch = 0;
    while (true)
    {
        if (row < this->srcLines.size())
        {
            string line = srcLines[row];
            if (col < line.size())
            {
                ch = line[col];
                col++;
                break;
            }
            else
            {
                row++;
                col = 0;
            }
        }
        else
        {
            break;
        }
    }
    return ch;
}

string Scanner::ch2String(char ch)
{
    string str = "a";
    str[0] = ch;
    return str;
}

bool Scanner::isDigit(const char &ch)
{
    return ch >= '0' && ch <= '9';
}

bool Scanner::isHexDigit(const char &ch)
{
    return isDigit(ch) || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F');
}

bool Scanner::isAlpha(const char &ch)
{
    return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_';
}

bool Scanner::isIntSuffix(const char &ch)
{
    return ch == 'L' || ch == 'l' || ch == 'u' || ch == 'U';
}

bool Scanner::isAlphaOrDigit(const char &ch) const
{
    return this->isDigit(ch) || this->isAlpha(ch);
}

string Scanner::genToken(const string &lexme, const string &type)
{
    return genToken(lexme, type, this->col - 1, this->row);
}

string Scanner::genToken2(const string &lexme, const string &type)
{
    return genToken(lexme, type, this->col - 2, this->row);
}

string Scanner::genToken(const string &lexme, const string &type, int icol, int irow)
{
    string strToken;

    if (icol <= 0)
    {
        irow--;
        icol = this->srcLines[irow].length();
    }
    strToken += "[@" + to_string(iTknNum) + "," + to_string(icol - lexme.size() + 1) + ":" + to_string(icol) + "='";
    strToken += lexme + "',<" + type + ">," + to_string(irow + 1) + ":" + to_string(icol - lexme.size() + 1) + "]\n";
    iTknNum++;
    return strToken;
}

void Scanner::run(string &inputFilePath)
{
    cout << "Scanning..." << endl;

    string strTokens;                    // all of Tokens
    DFA_STATE state = DFA_STATE_INITIAL; // state
    string lexme;                        // token lexme
    string val;                          // token val
    char ch = ' ';                       // next char
    bool keep = false;                   // keep current char
    bool end = false;                    // end current token

    readFile(inputFilePath);

    while (!end) //scanning loop
    {
        if (!keep)
        {
            ch = getNextChar();
        }

        keep = false;

        switch (state)
        {
        case DFA_STATE_INITIAL:
            lexme = "";

            if (ch == 'L' || ch == 'U' || ch == 'u')
            {
                state = DFA_STATE_CHAR_STR_PREFIX;
                lexme += ch;
            }
            else if (isAlpha(ch))
            {
                state = DFA_STATE_ID_0;
                lexme += ch;
            }
            else if (isDigit(ch))
            {
                val = "";
                if (ch == '0')
                    state = DFA_STATE_INT_OH;
                else
                    state = DFA_STATE_INT_D_0;
                val += ch;
            }
            else if (ch == '\'')
            {
                lexme += ch;
                state = DFA_STATE_CHAR;
            }
            else if (ch == '\"')
            {
                lexme += ch;
                state = DFA_STATE_STR;
            }
            else if (this->nonPrefixOperator.count(ch))
            {
                strTokens += genToken(ch2String(ch), "'" + ch2String(ch) + "'");
                state = DFA_STATE_INITIAL;
            }
            else if (ch == '+')
                state = DFA_STATE_ADD;
            else if (ch == '-')
                state = DFA_STATE_MINUS;
            else if (ch == '&')
                state = DFA_STATE_AND;
            else if (ch == '*')
                state = DFA_STATE_MUT;
            else if (ch == '!')
                state = DFA_STATE_SIGN;
            else if (ch == '/')
                state = DFA_STATE_DIVI;
            else if (ch == '%')
                state = DFA_STATE_PERCENT_0;
            else if (ch == '<')
                state = DFA_STATE_LESS_0;
            else if (ch == '>')
                state = DFA_STATE_GREATER_0;
            else if (ch == '=')
                state = DFA_STATE_EQUAL;
            else if (ch == '^')
                state = DFA_STATE_POWER;
            else if (ch == '|')
                state = DFA_STATE_OR;
            else if (ch == ':')
                state = DFA_STATE_COLON;
            else if (ch == '#')
                state = DFA_STATE_HASHTAG;
            else if (ch == ' ')
            {
            }
            else if (ch == 0)
            {
                col = 5;
                strTokens += genToken("<EOF>", "EOF");
                end = true;
            }
            break;
        case DFA_STATE_CHAR_STR_PREFIX:
            if (ch == '\'')
            {
                lexme += ch;
                state = DFA_STATE_CHAR;
            }
            else if (ch == '8')
            {
                lexme += ch;
            }
            else if (ch == '\"')
            {
                lexme += ch;
                state = DFA_STATE_STR;
            }
            else
            {
                state = DFA_STATE_ID_0;
                keep = true;
            }
            break;
        case DFA_STATE_ID_0:
            if (isAlphaOrDigit(ch))
            {
                lexme += ch;
            }
            else
            {
                if (keywordSet.count(lexme))
                    strTokens += genToken2(lexme, "'" + lexme + "'");
                else
                    strTokens += genToken2(lexme, "Identifier");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_INT_OH: // 确定是十六进制还是八进制
            if (ch == 'x' || ch == 'X')
            {
                val += ch;
                state = DFA_STATE_INT_H_0;
            }
            else if (isDigit(ch) && ch != '8' && ch != '9')
            {
                val += ch;
                state = DFA_STATE_INT_O_0;
            }
            else if (ch == '.')
            {
                val += ch;
                state = DFA_STATE_FLOAT_D_0;
            }
            else if (isIntSuffix(ch))
            {
                state = DFA_STATE_INT_SUFFIX_INITIAL;
                keep = true;
            }
            else
            {
                strTokens += genToken2(val, "IntegerConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_INT_D_0: // 匹配十进制数
            if (isDigit(ch))
            {
                val += ch;
            }
            else if (isIntSuffix(ch))
            {
                state = DFA_STATE_INT_SUFFIX_INITIAL;
                keep = true;
            }
            else if (ch == '.')
            {
                val += ch;
                state = DFA_STATE_FLOAT_D_0;
            }
            else if (ch == 'e' || ch == 'E')
            {
                val += ch;
                state = DFA_STATE_FLOAT_D_1;
            }
            else
            {
                strTokens += genToken2(val, "IntegerConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_INT_H_0: // 匹配十六进制数
            if (isHexDigit(ch))
            {
                val += ch;
            }
            else if (ch == '.')
            {
                val += ch;
                state = DFA_STATE_FLOAT_H_0;
            }
            else if (ch == 'p' || ch == 'P')
            {
                val += ch;
                state = DFA_STATE_FLOAT_H_1;
            }
            else if (isIntSuffix(ch))
            {
                state = DFA_STATE_INT_SUFFIX_INITIAL;
                keep = true;
            }
            else
            {
                strTokens += genToken2(val, "IntegerConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_INT_O_0: // 匹配八进制数
            if (isDigit(ch) && ch != '8' && ch != '9')
            {
                val += ch;
            }
            else if (isIntSuffix(ch))
            {
                state = DFA_STATE_INT_SUFFIX_INITIAL;
                keep = true;
            }
            else
            {
                strTokens += genToken2(val, "IntegerConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        // 匹配整型后缀
        case DFA_STATE_INT_SUFFIX_INITIAL:
            lexme = "";
            if (ch == 'L')
            {
                state = DFA_STATE_INT_SUFFIX_1_1;
                lexme += ch;
            }
            else if (ch == 'l')
            {
                state = DFA_STATE_INT_SUFFIX_1_2;
                lexme += ch;
            }
            else if (ch == 'u' || ch == 'U')
            {
                state = DFA_STATE_INT_SUFFIX_1_3;
                lexme += ch;
            }
            break;
        case DFA_STATE_INT_SUFFIX_1_1:
            if (ch == 'L')
            {
                state = DFA_STATE_INT_SUFFIX_2_1;
                lexme += ch;
            }
            else if (ch == 'u' || ch == 'U')
            {
                lexme += ch;
                strTokens += genToken(val + lexme, "IntegerConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val + lexme, "IntegerConstant");
                keep = true;
                state = DFA_STATE_INITIAL;
            }
            break;
        case DFA_STATE_INT_SUFFIX_1_2:
            if (ch == 'l')
            {
                state = DFA_STATE_INT_SUFFIX_2_1;
                lexme += ch;
            }
            else if (ch == 'u' || ch == 'U')
            {
                lexme += ch;
                strTokens += genToken(val + lexme, "IntegerConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val + lexme, "IntegerConstant");
                keep = true;
                state = DFA_STATE_INITIAL;
            }
            break;
        case DFA_STATE_INT_SUFFIX_1_3:
            if (ch == 'L')
            {
                state = DFA_STATE_INT_SUFFIX_2_2;
                lexme += ch;
            }
            else if (ch == 'l')
            {
                state = DFA_STATE_INT_SUFFIX_2_3;
                lexme += ch;
            }
            else
            {
                strTokens += genToken2(val + lexme, "IntegerConstant");
                keep = true;
                state = DFA_STATE_INITIAL;
            }
            break;
        case DFA_STATE_INT_SUFFIX_2_1:
            if (ch == 'u' || ch == 'U')
            {
                lexme += ch;
                strTokens += genToken(val + lexme, "IntegerConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val + lexme, "IntegerConstant");
                keep = true;
                state = DFA_STATE_INITIAL;
            }
            break;
        case DFA_STATE_INT_SUFFIX_2_2:
            if (ch == 'L')
            {
                lexme += ch;
                strTokens += genToken(val + lexme, "IntegerConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val + lexme, "IntegerConstant");
                keep = true;
                state = DFA_STATE_INITIAL;
            }
            break;
        case DFA_STATE_INT_SUFFIX_2_3:
            if (ch == 'l')
            {
                lexme += ch;
                strTokens += genToken(val + lexme, "IntegerConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val + lexme, "IntegerConstant");
                keep = true;
                state = DFA_STATE_INITIAL;
            }
            break;
        // 匹配十进制浮点数
        case DFA_STATE_FLOAT_D_0:
            if (isDigit(ch))
            {
                val += ch;
            }
            else if (ch == 'e' || ch == 'E')
            {
                val += ch;
                state = DFA_STATE_FLOAT_D_1;
            }
            else if (ch == 'f' || ch == 'l' || ch == 'F' || ch == 'L')
            {
                val += ch;
                strTokens += genToken(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_FLOAT_D_1:
            if (isDigit(ch) || ch == '+' || ch == '-')
            {
                val += ch;
                state = DFA_STATE_FLOAT_D_2;
            }
            else if (ch == 'f' || ch == 'l' || ch == 'F' || ch == 'L')
            {
                val += ch;
                strTokens += genToken(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_FLOAT_D_2:
            if (isDigit(ch))
            {
                val += ch;
            }
            else if (ch == 'f' || ch == 'l' || ch == 'F' || ch == 'L')
            {
                val += ch;
                strTokens += genToken(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        // 匹配十六进制浮点数
        case DFA_STATE_FLOAT_H_0:
            if (isHexDigit(ch))
            {
                val += ch;
            }
            else if (ch == 'p' || ch == 'P')
            {
                val += ch;
                state = DFA_STATE_FLOAT_H_1;
            }
            else if (ch == 'f' || ch == 'l' || ch == 'F' || ch == 'L')
            {
                val += ch;
                strTokens += genToken(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_FLOAT_H_1:
            if (isHexDigit(ch) || ch == '+' || ch == '-')
            {
                val += ch;
                state = DFA_STATE_FLOAT_H_2;
            }
            else if (ch == 'f' || ch == 'l' || ch == 'F' || ch == 'L')
            {
                val += ch;
                strTokens += genToken(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_FLOAT_H_2:
            if (isHexDigit(ch))
            {
                val += ch;
            }
            else if (ch == 'f' || ch == 'l' || ch == 'F' || ch == 'L')
            {
                val += ch;
                strTokens += genToken(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                strTokens += genToken2(val, "FloatingConstant");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        // 匹配字符常量
        case DFA_STATE_CHAR:
            if (ch == '\'')
            {
                lexme += ch;
                strTokens += genToken(lexme, "CharacterConstant");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                lexme += ch;
            }
            break;
        // 匹配字符串字面量
        case DFA_STATE_STR:
            if (ch == '\"')
            {
                lexme += ch;
                strTokens += genToken(lexme, "StringLiteral");
                state = DFA_STATE_INITIAL;
            }
            else
            {
                lexme += ch;
            }
            break;
        // 匹配运算符
        case DFA_STATE_ADD:
            if (ch == '+')
            {
                strTokens += genToken("++", "'++'");
            }
            else if (ch == '=')
            {
                strTokens += genToken("+=", "'+='");
            }
            else
            {
                strTokens += genToken2("+", "'+'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_MINUS:
            if (ch == '-')
            {
                strTokens += genToken("--", "'--'");
            }
            else if (ch == '>')
            {
                strTokens += genToken("->", "'->'");
            }
            else if (ch == '=')
            {
                strTokens += genToken("-=", "'-='");
            }
            else
            {
                strTokens += genToken2("-", "'-'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_AND:
            if (ch == '&')
            {
                strTokens += genToken("&&", "'&&'");
            }
            else if (ch == '=')
            {
                strTokens += genToken("&=", "'&='");
            }
            else
            {
                strTokens += genToken("&", "'&'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_MUT:
            if (ch == '=')
            {
                strTokens += genToken("*=", "'*='");
            }
            else
            {
                strTokens += genToken2("*", "'*'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_SIGN:
            if (ch == '=')
            {
                strTokens += genToken("!=", "'!='");
            }
            else
            {
                strTokens += genToken2("!", "'!'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_DIVI:
            if (ch == '=')
            {
                strTokens += genToken("/=", "'/='");
            }
            else
            {
                strTokens += genToken2("/", "'/'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_PERCENT_0:
            if (ch == '=')
            {
                strTokens += genToken("%=", "'%='");
                state = DFA_STATE_INITIAL;
            }
            else if (ch == '>')
            {
                strTokens += genToken("%>", "'%>'");
                state = DFA_STATE_INITIAL;
            }
            else if (ch == ':')
            {
                state = DFA_STATE_PERCENT_1;
                lexme += ch;
            }
            else
            {
                strTokens += genToken2("%", "'%'");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_PERCENT_1:
            if (ch == '%')
            {
                state = DFA_STATE_PERCENT_2;
                lexme += ch;
            }
            else
            {
                strTokens += genToken2("%:", "'%:'");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_PERCENT_2:
            if (ch == ':')
            {
                strTokens += genToken("%:%:", "'%:%:'");
            }
            else if (ch == '=' || ch == '>')
            {
                strTokens += genToken("%:", "'%:'", col - 3, row);
                strTokens += genToken(ch2String('%') + ch, ch2String('\'') + '%' + ch + "'");
            }
            else
            {
                strTokens += genToken("%:", "'%:'", col - 3, row);
                strTokens += genToken2("%", "'%'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_LESS_0:
            if (ch == '=' || ch == '%' || ch == ':')
            {
                strTokens += genToken(ch2String('<') + ch, ch2String('\'') + '<' + ch + "'");
                state = DFA_STATE_INITIAL;
            }
            else if (ch == '<')
            {
                lexme += ch;
                state = DFA_STATE_LESS_1;
            }
            else
            {
                strTokens += genToken2("<", "'<'");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_LESS_1:
            if (ch == '=')
            {
                strTokens += genToken("<<=", "'<<='");
            }
            else
            {
                strTokens += genToken2("<<", "'<<'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_GREATER_0:
            if (ch == '=')
            {
                strTokens += genToken(">=", "'>='");
                state = DFA_STATE_INITIAL;
            }
            else if (ch == '>')
            {
                lexme += ch;
                state = DFA_STATE_GREATER_1;
            }
            else
            {
                strTokens += genToken2(">", "'>'");
                state = DFA_STATE_INITIAL;
                keep = true;
            }
            break;
        case DFA_STATE_GREATER_1:
            if (ch == '=')
            {
                strTokens += genToken(">>=", "'>>='");
            }
            else
            {
                strTokens += genToken2(">>", "'>>'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_EQUAL:
            if (ch == '=')
            {
                strTokens += genToken("==", "'=='");
            }
            else
            {
                strTokens += genToken("=", "'='");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_POWER:
            if (ch == '=')
            {
                strTokens += genToken("^=", "'^='");
            }
            else
            {
                strTokens += genToken2("^", "'^'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_OR:
            if (ch == '|' || ch == '=')
            {
                strTokens += genToken(ch2String('|') + ch, ch2String('\'') + '|' + ch + "'");
            }
            else
            {
                strTokens += genToken2("|", "'|'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_COLON:
            if (ch == '>')
            {
                strTokens += genToken(":>", "':>'");
            }
            else
            {
                strTokens += genToken2(":", "':'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;
        case DFA_STATE_HASHTAG:
            if (ch == '#')
            {
                strTokens += genToken("##", "'##'");
            }
            else
            {
                strTokens += genToken2("#", "'#'");
                keep = true;
            }
            state = DFA_STATE_INITIAL;
            break;

        default:
            cout << "[ERROR]Scanner:line " << row << ", column=" << col << ", unreachable state!" << endl;
            break;
        }
    }

    writeFile(strTokens, inputFilePath);
}

int main(int argc, char const *argv[])
{
    if (argc != 2)
        return 1;

    Scanner sc;
    string filename = argv[1];
    sc.run(filename);
    return 0;
}
