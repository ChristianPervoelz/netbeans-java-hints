/*
 *  Copyright 2016 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: cpervoelz
 *
 *  $Id$
 */

package de.cp.netbeans.supplemental.hints.toomanyof;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.util.TreeScanner;

/**
 * Visitor implementation to count break and continue statements.
 *
 * @version 0.2
 * @author cperv
 * @since 1.0
 */
public class TooManyOfCounterVisitor extends TreeScanner<Integer, Integer>{

  private int returns = 0;
  private int breaksAndConts = 0;

  @Override
  public Integer visitReturn(ReturnTree node, Integer p) {
    returns++;
    return super.visitReturn(node, p);
  }

  @Override
  public Integer visitBreak(BreakTree node, Integer p) {
    breaksAndConts++;
    return super.visitBreak(node, p);
  }

  @Override
  public Integer visitContinue(ContinueTree node, Integer p) {
    breaksAndConts++;
    return super.visitContinue(node, p);
  }

  int getReturns() {
    return returns;
  }

  int getBreaksAndConts() {
    return breaksAndConts;
  }

}
