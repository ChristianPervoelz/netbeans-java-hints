package de.cp.netbeans.supplemental.hints.complexity;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreeScanner;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * TreeScanner implementation to calculate the coginitve complexity as described here: https://www.sonarsource.com/docs/CognitiveComplexity.pdf.
 *
 * <p>
 * This class is based on https://github.com/SonarSource/sonar-java/blob/master/java-checks/src/main/java/org/sonar/java/checks/CognitiveComplexityMethodCheck.java.
 * </p>
 *
 * @version 0.2
 * @author cperv
 * @since 1.0
 */
public class CognitiveComplexityVisitor extends TreeScanner<Integer, Integer> {

  private final Collection<Tree> ignored;
  private final Map<Tree, String> penalties;
  int complexity;
  int nesting;
  boolean ignoreNesting;
  private final boolean addReason;

  CognitiveComplexityVisitor(final boolean addReason) {
    complexity = 0;
    nesting = 1;
    ignoreNesting = false;
    ignored = new HashSet<>();
    penalties = new HashMap<>();
    this.addReason = addReason;
  }

  private void increaseComplexityByNesting(final Tree tree) {
    increaseComplexity(tree, nesting);
  }

  private void increaseComplexityByOne(final Tree tree) {
    increaseComplexity(tree, 1);
  }

  private void increaseComplexity(final Tree tree, final int increase) {
    complexity += increase;
    if (ignoreNesting) {
      penalties.put(tree, "+1" + getAdditionalText(tree));
      ignoreNesting = false;
    } else if (!ignored.contains(tree)) {
      String message = "+" + increase + getAdditionalText(tree);
      if (increase > 1) {
        message += " (incl " + (increase - 1) + " for nesting)";
      }
      penalties.put(tree, message);
    }
  }

  private String getAdditionalText(final Tree tree) {
    String ret = "";

    if(addReason) {
      final String name = getName(tree);
      if(!name.isEmpty()) {
        ret = " (" + name + ")";
      }
    }

    return ret;
  }

  @Override
  public Integer visitIf(IfTree node, Integer p) {
    increaseComplexityByNesting(node);
    scan(node.getCondition(), p);
    nesting++;
    scan(node.getThenStatement(), p);
    nesting--;

    final StatementTree elseStatement = node.getElseStatement();
    boolean elseStatementNotIF = elseStatement != null && elseStatement.getKind() != Kind.IF;
    if (elseStatementNotIF) {
      increaseComplexityByOne(elseStatement);
      nesting++;
    } else if (elseStatement != null) {
      // else statement is an if, visiting it will increase complexity by nesting so by one only.
      ignoreNesting = true;
      complexity -= nesting - 1;
    }
    scan(elseStatement, p);
    if (elseStatementNotIF) {
      nesting--;
    }

    return p;
  }

  @Override
  public Integer visitTry(TryTree node, Integer p) {
    scan(node.getResources(), p);
    scan(node.getBlock(), p);
    node.getCatches().forEach(c -> increaseComplexityByNesting(c));
    nesting++;
    scan(node.getCatches(), p);
    nesting--;
    scan(node.getFinallyBlock(), p);

    return p;
  }

  @Override
  public Integer visitForLoop(ForLoopTree tree, Integer p) {
    increaseComplexityByNesting(tree);
    nesting++;
    super.visitForLoop(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitEnhancedForLoop(EnhancedForLoopTree tree, Integer p) {
    increaseComplexityByNesting(tree);
    nesting++;
    super.visitEnhancedForLoop(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitWhileLoop(WhileLoopTree tree, Integer p) {
    increaseComplexityByNesting(tree);
    nesting++;
    super.visitWhileLoop(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitDoWhileLoop(DoWhileLoopTree tree, Integer p) {
    increaseComplexityByNesting(tree);
    nesting++;
    super.visitDoWhileLoop(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitConditionalExpression(ConditionalExpressionTree tree, Integer p) {
    increaseComplexityByNesting(tree);
    nesting++;
    super.visitConditionalExpression(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitSwitch(SwitchTree tree, Integer p) {
    increaseComplexityByNesting(tree);
    nesting++;
    super.visitSwitch(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitBreak(BreakTree tree, Integer p) {
    if (tree.getLabel() != null) {
      increaseComplexityByOne(tree);
    }
    super.visitBreak(tree, p);
    return p;
  }

  @Override
  public Integer visitContinue(ContinueTree tree, Integer p) {
    if (tree.getLabel() != null) {
      increaseComplexityByOne(tree);
    }
    super.visitContinue(tree, p);
    return p;
  }

  @Override
  public Integer visitClass(ClassTree tree, Integer p) {
    nesting++;
    super.visitClass(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitLambdaExpression(LambdaExpressionTree tree, Integer p) {
    nesting++;
    super.visitLambdaExpression(tree, p);
    nesting--;
    return p;
  }

  @Override
  public Integer visitBinary(BinaryTree tree, Integer p) {
    if (Kind.CONDITIONAL_AND == tree.getKind() || Kind.CONDITIONAL_OR == tree.getKind()) {
      increaseComplexityByOne(tree);
      ExpressionTree left = tree.getLeftOperand();

      if (left.getKind() == tree.getKind()) {
        ignored.add(left);
        complexity--;
      }
      ExpressionTree right = tree.getRightOperand();
      if (right.getKind() == tree.getKind()) {
        ignored.add(right);
        complexity--;
      }
    }
    super.visitBinary(tree, p);
    return p;
  }

  private String getName(final Tree tree) {
    String ret;
    switch (tree.getKind()) {
      case LAMBDA_EXPRESSION:
        ret = "Lambda expression";
        break;
      case BLOCK:
        ret = "else block";
        break;
      case CLASS:
        ret = "Nested class";
        break;
      case CONDITIONAL_EXPRESSION:
        ret = "Conditional";
        break;
      case CONDITIONAL_AND:
        ret = "&&";
        break;
      case CONDITIONAL_OR:
        ret = "||";
        break;
      case CONTINUE:
        ret = "Continue";
        break;
      case DO_WHILE_LOOP:
        ret = "Do-While-Loop";
        break;
      case ENHANCED_FOR_LOOP:
        ret = "Enhanced For-Loop";
        break;
      case FOR_LOOP:
        ret = "For-Loop";
        break;
      case IF:
        ret = "If";
        break;
      case SWITCH:
        ret = "Switch";
        break;
      case TRY:
        ret = "try-catch";
        break;
      case WHILE_LOOP:
        ret = "While-Loop";
        break;
      default:
        ret = tree.getKind().toString();
    }

    return ret;
  }

  public int getComplexity() {
    return complexity;
  }

  public Map<Tree, String> getPenalties() {
    return penalties;
  }
}
