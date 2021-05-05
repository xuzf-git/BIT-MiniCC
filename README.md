# BIT-MiniCC

编译原理课程实践，设计实现C语言编译器。

参考老师提供的框架，实现完整C语言语法的子集。

## 1 Todo List
- [ ] 预处理 

- [x] 词法分析
- [x] 语法分析
- [ ] 语义分析
- [ ] 中间代码生成
- [ ] 代码优化
- [ ] 代码生成

## 2 实现思路
### 2.1 词法分析
> 对应代码: [src/bit/minisys/minicc/scanner/Scanner.cpp](src/bit/minisys/minicc/scanner/Scanner.cpp)

设计有限状态自动机（DFA），使用程序中心法实现。
目前能正确识别标识符、关键字、常量（整型、浮点型、字符常量、字符串字面量）、符号（运算符、限定符）

### 2.2 语法分析

> 对应代码: [src/bit/minisys/minicc/parser](src/bit/minisys/minicc/parser)

使用 ANTLR 实现，使用ANTLR文法编写 g4 语法规则文件

1. 根据语法规则生成分析器，生成Lexer、Parser、Listener

```shell
antlr4 src/bit/minisys/minicc/parser/C.g4
```

2. 定义AST节点，放在 [src\bit\minisys\minicc\parser\ast](src\bit\minisys\minicc\parser\ast)

3. ANTLR 提供了Listener和visitor两种模式，实现对语法解析树ParserTree的遍历：

   > Listener模式是ANTLR提供的对解析树的一种遍历模式。使用ParseTreeWalker遍历树的过程中（深度优先），每次进入和退出规则节点时，触发对应的enterRule/exitRule方法。
   >
   > 本实验使用Listener接口，将ParserTree转换成AST，步骤如下
   >
   > 1. 定义映射表ParseTreeProperty astMap。
   > 2. 在遍历ParseTree退出规则节点时，以当前的PareTree为key,以对应的ASTNode为value构造键值对，插入到映射表astMap中。
   > 3. 其中，在构造ASTNode时，可以通过astMap查找它的子节点对应的ASTNode实现当前ASTNode的构造。即：astMap中存储了ParseTree中的规则节点的“历史”信息。、
   > 4. 最终文法公理的规则节点Start对应的ASTNode——ASTCompilationUnit就是对应AST的根节点。

4. AST的序列化为json