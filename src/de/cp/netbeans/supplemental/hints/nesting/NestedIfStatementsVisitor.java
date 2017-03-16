package de.cp.netbeans.supplemental.hints.nesting;

import java.util.ArrayDeque;
import java.util.Deque;

import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreeScanner;

public class NestedIfStatementsVisitor extends TreeScanner<Integer, Integer> {

  private final Deque<Tree> nestingLevel;

  private Tree treeReachingNesting;
  private final int max;

  NestedIfStatementsVisitor(final int max) {
    this.max = max;
    nestingLevel = new ArrayDeque<>();
  }

  @Override
  public Integer visitIf(final IfTree tree, final Integer val) {
    checkNesting(tree);
    nestingLevel.push(tree);
    visit(tree, val);
    nestingLevel.pop();
    return val;
  }

  @Override
  public Integer visitForLoop(ForLoopTree tree, Integer val) {
    checkNesting(tree);
    nestingLevel.push(tree);
    super.visitForLoop(tree, val);
    nestingLevel.pop();
    return val;
  }

  @Override
  public Integer visitEnhancedForLoop(EnhancedForLoopTree node, Integer p) {
    checkNesting(node);
    nestingLevel.push(node);
    super.visitEnhancedForLoop(node, p);
    nestingLevel.pop();
    return p;
  }

  @Override
  public Integer visitWhileLoop(WhileLoopTree node, Integer p) {
    checkNesting(node);
    nestingLevel.push(node);
    super.visitWhileLoop(node, p);
    nestingLevel.pop();
    return p;
  }

  @Override
  public Integer visitDoWhileLoop(DoWhileLoopTree node, Integer p) {
    checkNesting(node);
    nestingLevel.push(node);
    super.visitDoWhileLoop(node, p);
    nestingLevel.pop();
    return p;
  }

  @Override
  public Integer visitSwitch(SwitchTree node, Integer p) {
    checkNesting(node);
    nestingLevel.push(node);
    super.visitSwitch(node, p);
    nestingLevel.pop();
    return p;
  }

  @Override
  public Integer visitTry(TryTree node, Integer p) {
    checkNesting(node);
    nestingLevel.push(node);
    scan(node.getBlock(), p);
    nestingLevel.pop();
    scan(node.getResources(), p);
    scan(node.getCatches(), p);
    scan(node.getFinallyBlock(), p);
    return p;
  }

  Tree getReachingTree() {
    return treeReachingNesting;
  }

  private void checkNesting(Tree tree) {
    int size = nestingLevel.size();
    if (size == max) {
      treeReachingNesting = tree;
    }
  }

  private void visit(IfTree tree, Integer p) {
    scan(tree.getCondition(), p);
    scan(tree.getThenStatement(), p);

    StatementTree elseStatementTree = tree.getElseStatement();
    if (elseStatementTree != null && Tree.Kind.IF == elseStatementTree.getKind()) {
      visit((IfTree) elseStatementTree, p);
    } else {
      scan(elseStatementTree, p);
    }
  }

}
