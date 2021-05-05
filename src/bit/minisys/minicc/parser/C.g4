grammar C;

// parser
// 1. 表达式
// 1.1 基础表达式
primaryExpression
    : Identifier
    | IntConstant
    | FloatConstant
    | CharConstant
    | StringLiteral+
    | '(' expression ')'
    ;
// 1.2 后缀表达式
postfixExpression
    : primaryExpression
    | postfixExpression '[' expression ']'  // 数组下标访问
    | postfixExpression '(' argumentExpressionList? ')'  // 函数调用
    | postfixExpression '.' Identifier // struct 或者 union 成员
    | postfixExpression '->' Identifier
    | postfixExpression '++' // 后缀自增运算
    | postfixExpression '--' // 后缀自减运算
    | '(' typeName ')' '{' initializerList '}'
    | '(' typeName ')' '{' initializerList ',' '}'
    ;
// 1.3 表达式列表
argumentExpressionList
    : assignmentExpression
    | argumentExpressionList ',' assignmentExpression
    ;
// 1.4 一元表达式
unaryExpression
    : postfixExpression
    | '++' unaryExpression
    | '--' unaryExpression
    | unaryOpeartor castExpression
    | 'sizeof' unaryExpression
    | 'sizeof' typeName
    ;
unaryOpeartor : '&' | '*' | '+' | '-' | '~' | '!';
// 1.5 强制类型转化
castExpression
    : unaryExpression
    | '(' typeName ')' castExpression
    ;
// 1.6 乘法优先级表达式
multiplicationExpression
    : castExpression
    | multiplicationExpression '*' castExpression
    | multiplicationExpression '/' castExpression
    | multiplicationExpression '%' castExpression
    ;
// 1.7 加法优先级表达式
addExpression
    : multiplicationExpression
    | addExpression '+' multiplicationExpression
    | addExpression '-' multiplicationExpression
    ;
// 1.8 移位运算优先级表达式
shiftExpression
    : addExpression
    | shiftExpression '<<' addExpression
    | shiftExpression '>>' addExpression
    ;
// 1.9 关系表达式
relationExpression
    : shiftExpression
    | relationExpression '<' shiftExpression
    | relationExpression '>' shiftExpression
    | relationExpression '<=' shiftExpression
    | relationExpression '>=' shiftExpression
    ;
// 1.10 判等表达式
equalExpression
    : relationExpression
    | equalExpression '==' relationExpression
    | equalExpression '!=' relationExpression
    ;
// 1.11 与操作
andExpression
    : equalExpression
    | andExpression '&' equalExpression
    ;
// 1.12 异或操作
xorExpression
    : andExpression
    | xorExpression '^' andExpression
    ;
// 1.14 或运算
inOrExpression
    : xorExpression
    | inOrExpression '|' xorExpression
    ;
// 1.15 逻辑与运算
logicalAndExpression
    : inOrExpression
    | logicalAndExpression '&&' inOrExpression
    ;
// 1.16 逻辑或运算
logicalOrExpression
    : logicalAndExpression
    | logicalOrExpression '||' logicalAndExpression
    ;
// 1.17 条件表达式
conditionExpression
    : logicalOrExpression
    | logicalOrExpression '?' expression ':' conditionExpression
    ;
// 1.18 赋值表达式
assignmentExpression
    : conditionExpression
    | unaryExpression assignmentOpeartor assignmentExpression
    ;
// 1.19 赋值运算符
assignmentOpeartor
    :  '=' | '*=' | '/=' | '%=' | '+=' | '-=' | '<<=' | '>>=' | '&=' | '^=' | '|='
    ;
// 1.20 表达式
expression
    : assignmentExpression
    | expression ',' assignmentExpression
    ;

// 2 声明语句
declaration
    : declarationSpecifierList initDeclaratorList? ';'
    ;
// 2.1 声明语句说明符
declarationSpecifierList
    : (storageClassSpecifier | typeSpecifier | typeQualifier | functionSpecifier) declarationSpecifierList?
    ;
// 2.2 带初始化的声明列表
initDeclaratorList
    : initDeclarator
    | initDeclaratorList ',' initDeclarator
    ;
// 2.3 带初始化的声明语句
initDeclarator
    : declarator
    | declarator '=' initializer
    ;
// 2.4 存储类型说明符
storageClassSpecifier
    : Typedef
    | Extern
    | Static
    | Auto
    ;
// 2.5 类型说明符
typeSpecifier
    : Void
    | Char
    | Short
    | Int
    | Long
    | Float
    | Double
    | Signed
    | Unsigned
//    | typedefName
    ;
// 2.6 类型限定符
typeQualifier
    : Const
    | Restrict
    | Volatile
    ;
// 2.7 函数说明符
functionSpecifier : Inline ;
// 2.8 声明符
declarator
    : pointer? directDeclarator
    ;
// 2.9 直接声明
directDeclarator
    : Identifier
    | '(' declarator ')'
    | directDeclarator '[' typeQualifier* assignmentExpression? ']'
    | directDeclarator '[' Static typeQualifier* assignmentExpression ']'
    | directDeclarator '[' typeQualifier+ Static assignmentExpression ']'
    | directDeclarator '[' typeQualifier* '*' ']'
    | directDeclarator '(' parameterTypeList ')'
    | directDeclarator '(' Identifier* ')'
    ;
// 2.10 指针类型
pointer
    : '*' typeQualifier*
    | '*' typeQualifier* pointer
    ;
// 2.11 函数的参数列表
parameterTypeList
    : parameterList
    | parameterList ',' '...'
    ;
parameterList
    : parameterDeclaration
    | parameterList ',' parameterDeclaration
    ;
parameterDeclaration
    : declarationSpecifierList declarator
    ;
// 2.12 typeName
typeName
    : (typeSpecifier | typeQualifier)+ pointer?
    ;
// 2.13 typedef
typedefName
    : Identifier
    ;
// 2.14 初始化元素
initializer
    : assignmentExpression
    | '{' initializerList '}'
    | '{' initializerList ',' '}'
    ;
// 2.15 初始化列表
initializerList
    : designation? initializer
    | initializerList ',' designation? initializer
    ;
designation
    : designatorList '='
    ;
designatorList
    : designator
    | designatorList designator
    ;
designator
    : '[' conditionExpression ']'
    | '.' Identifier
    ;

// 3 语句块
statement
    : labeledStatement
    | compoundStatement
    | expressionStatement
    | selectionStatement
    | iterationStatement
    | jumpStatement
    ;
// 3.1 命名语句块
labeledStatement
    : Identifier ':' statement
    | Case conditionExpression ':' statement
    | Default ':' statement
    ;
// 3.2 复合语句块
compoundStatement
    : '{' blockItemList? '}'
    ;
blockItemList
    : blockItem
    | blockItemList blockItem
    ;
blockItem
    : statement
    | declaration
    ;
// 3.3 表达式语句块
expressionStatement
    : expression? ';'
    ;
// 3.4 分支选择语句块
selectionStatement
    : If '(' expression ')' statement
    | If '(' expression ')' statement (Else statement)
    | Switch '(' expression ')' statement
    ;
// 3.5 迭代语句块
iterationStatement
    : While '(' expression ')' statement
    | Do statement While '(' expression ')' ';'
    | For '(' expression? ';' expression? ';' expression? ')' statement
    | For '(' declaration expression? ';' expression? ')' statement
    ;
// 3.6 跳转语句
jumpStatement
    : Goto Identifier ';'
    | Continue ';'
    | Break ';'
    | Return expression? ';'
    ;
// 文法处理单元
translationUnit
    : externalDeclaration
    | translationUnit externalDeclaration
    ;
externalDeclaration
    : functionDefinition
    | declaration
    ;
// 函数定义
functionDefinition
    :  declarationSpecifierList? declarator declarationList? compoundStatement
    ;
// 声明列表
declarationList
    : declaration
    | declarationList declaration
    ;

// 4 文法公理
start
    : translationUnit? EOF
    ;


// lexer
// key word
Auto : 'auto';
Break : 'break';
Case : 'case';
Char : 'char';
Const : 'const';
Continue : 'continue';
Default : 'default';
Do : 'do';
Double : 'double';
Else : 'else';
Enum : 'enum';
Extern : 'extern';
Float : 'float';
For : 'for';
Goto : 'goto';
If : 'if';
Inline : 'inline';
Int : 'int';
Long : 'long';
Register : 'register';
Restrict : 'restrict';
Return : 'return';
Short : 'short';
Signed : 'signed';
Sizeof : 'sizeof';
Static : 'static';
Struct : 'struct';
Switch : 'switch';
Typedef : 'typedef';
Union : 'union';
Unsigned : 'unsigned';
Void : 'void';
Volatile : 'volatile';
While : 'while';
// 符号
LeftParen : '(';
RightParen : ')';
LeftBracket : '[';
RightBracket : ']';
LeftBrace : '{';
RightBrace : '}';

Less : '<';
LessEqual : '<=';
Greater : '>';
GreaterEqual : '>=';
LeftShift : '<<';
RightShift : '>>';

Plus : '+';
PlusPlus : '++';
Minus : '-';
MinusMinus : '--';
Star : '*';
Div : '/';
Mod : '%';

And : '&';
Or : '|';
AndAnd : '&&';
OrOr : '||';
Caret : '^';
Not : '!';
Tilde : '~';

Question : '?';
Colon : ':';
Semi : ';';
Comma : ',';
// 赋值
Assign : '=';
// 一元赋值
StarAssign : '*=';
DivAssign : '/=';
ModAssign : '%=';
PlusAssign : '+=';
MinusAssign : '-=';
LeftShiftAssign : '<<=';
RightShiftAssign : '>>=';
AndAssign : '&=';
XorAssign : '^=';
OrAssign : '|=';
// 判等
Equal : '==';
NotEqual : '!=';
// 成员访问
Arrow : '->';
Dot : '.';
Ellipsis : '...';
// 标识符
Identifier : [a-zA-Z_] [a-zA-Z_0-9]*;
// 整型常量
IntConstant
    : DecConstant IntSuffix?
    | BinConstant IntSuffix?
    | OctConstant IntSuffix?
    | HexConstant IntSuffix?
    ;
fragment
DecConstant : [1-9] [0-9] * ;
fragment
BinConstant : '0' [bB] [0-1]+ ;
fragment
OctConstant : '0' [0-7]* ;
fragment
HexConstant : '0' [xX] [0-9a-fA-F]+ ;
fragment
IntSuffix
    : [uU][lL]
    | [uU]'ll'
    | [uU]'LL'
    | [lL][uU]
    | 'll'[uU]
    | 'LL'[uU]
    ;
// 浮点型常量
FloatConstant
    : DecFloatConstant
    | HexFloatConstant
    ;
fragment
DecFloatConstant
    : FractionalConstant ExponentPart? FloatingSuffix?
    | [0-9]+ ExponentPart FloatingSuffix?
    ;
fragment
FractionalConstant
    : [0-9]* '.' [0-9]+
    | [0-9]+ '.'
    ;
fragment
ExponentPart
    :   'e' [+-]? [0-9]+
    |   'E' [+-]? [0-9]+
    ;
fragment
HexFloatConstant
    : ('0x' | '0X') HexFractionalConstant BinExponentPart FloatingSuffix?
    | ('0x' | '0X') [0-9a-fA-F]+ BinExponentPart FloatingSuffix?
    ;
fragment
HexFractionalConstant
    : [0-9a-fA-F]* '.' [0-9a-fA-F]+
    | [0-9a-fA-F]+ '.'
    ;
fragment
BinExponentPart
    :   'p' [+-]? [0-9]+
    |   'P' [+-]? [0-9]+
    ;
fragment
FloatingSuffix : 'f' | 'l' | 'F' | 'L' ;
// 字符常量
CharConstant : [uUL]? '\'' CChar+ '\'' ;
fragment
CChar
    : ~['\\\n\r]
    | '\\' ['"?abfnrtv\\]
    ;
// 字符串字面量
StringLiteral : ('u8' | 'u' | 'U' | 'L')? '"' (CChar | '\\\n' | '\\\r\n' | '\'')+ '"' ;
// 空白
Whitespace : [ \t]+ -> skip;
// 换行
Newline : ('\r' '\n'? | '\n') -> skip;