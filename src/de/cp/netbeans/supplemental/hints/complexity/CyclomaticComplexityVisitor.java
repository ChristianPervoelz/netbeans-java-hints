package de.cp.netbeans.supplemental.hints.complexity;


import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreeScanner;

/**
 * This class is responsible for computing the cyclomatic complexity of a given method.
 *
 * @author Krystian Warzocha
 */
public class CyclomaticComplexityVisitor extends TreeScanner<Integer, Integer> {

  private int throwStatements = 0;

  @Override
  public Integer visitForLoop(final ForLoopTree node, final Integer p) {
    int add = 1;
    if(Kind.PARENTHESIZED == node.getCondition().getKind()) {
      add += analyzeParenthesized(node.getCondition(), 0);
    }
    return super.visitForLoop(node, p) + add;
  }

  @Override
  public Integer visitEnhancedForLoop(final EnhancedForLoopTree node, final Integer p) {
    return super.visitEnhancedForLoop(node, p) + 1;
  }

  @Override
  public Integer visitWhileLoop(final WhileLoopTree node, final Integer p) {
    int add = 1;
    if(Kind.PARENTHESIZED == node.getCondition().getKind()) {
      add += analyzeParenthesized(node.getCondition(), 0);
    }
    return super.visitWhileLoop(node, p) + add;
  }

  @Override
  public Integer visitDoWhileLoop(final DoWhileLoopTree node, final Integer p) {
    int add = 1;
    if(Kind.PARENTHESIZED == node.getCondition().getKind()) {
      add += analyzeParenthesized(node.getCondition(), 0);
    }
    return super.visitDoWhileLoop(node, p) + add;
  }

  @Override
  public Integer visitIf(final IfTree node, final Integer p) {
    int add = 1;

    if(Kind.PARENTHESIZED == node.getCondition().getKind()) {
      add += analyzeParenthesized(node.getCondition(), 0);
    }

    return super.visitIf(node, p) + add;
  }

  private int analyzeParenthesized(final ExpressionTree tree, int val) {
    final ParenthesizedTree parenthesizedTree = (ParenthesizedTree)tree;
    final ExpressionTree ptExpression = parenthesizedTree.getExpression();
    final Kind ptKind = ptExpression.getKind();

    if(Kind.CONDITIONAL_OR == ptKind || Kind.CONDITIONAL_AND == ptKind) {
      val = analyzeConditional(ptExpression, val);
    } else if(Kind.PARENTHESIZED == ptKind) {
      val = analyzeParenthesized(ptExpression, val);
    }

    return val;
  }

  private int analyzeConditional(final ExpressionTree tree, int val) {
    final BinaryTree binaryExpression = (BinaryTree)tree;
    Kind kind = binaryExpression.getLeftOperand().getKind();

    if(Kind.CONDITIONAL_OR == kind || Kind.CONDITIONAL_AND == kind) {
      val = analyzeConditional(binaryExpression.getLeftOperand(), val);
    } else if(Kind.PARENTHESIZED == kind) {
      val = analyzeParenthesized(binaryExpression.getLeftOperand(), val);
    }

    kind = binaryExpression.getRightOperand().getKind();

    if(Kind.CONDITIONAL_OR == kind || Kind.CONDITIONAL_AND == kind) {
      val = analyzeConditional(binaryExpression.getRightOperand(), val);
    } else if(Kind.PARENTHESIZED == kind) {
      val = analyzeParenthesized(binaryExpression.getRightOperand(), val);
    }

    return val + 1;
  }

  @Override
  public Integer visitCase(final CaseTree node, final Integer p) {
    return super.visitCase(node, p) + 1;
  }

  @Override
  public Integer visitReturn(final ReturnTree node, final Integer p) {
    // just increase the value
    // if we call super.visitReturn(...) here, we'll get a big, fat NPE
    return p + 1;
  }

  @Override
  public Integer visitConditionalExpression(final ConditionalExpressionTree node, final Integer p) {
    return super.visitConditionalExpression(node, p) + 1;
  }

  @Override
  public Integer visitThrow(ThrowTree node, Integer p) {
    throwStatements++;
    return super.visitThrow(node, p) + 1;
  }

  @Override
  public Integer reduce(Integer r1, Integer r2) {
    return (r1 == null ? 0 : r1) + (r2 == null ? 0 : r2);
  }

  int getThrows() {
    return throwStatements;
  }
}
