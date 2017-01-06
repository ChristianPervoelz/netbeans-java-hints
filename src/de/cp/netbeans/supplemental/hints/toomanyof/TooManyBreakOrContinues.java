/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cp.netbeans.supplemental.hints.toomanyof;

import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_TooManyBreakOrContinues", description = "#DESC_TooManyBreakOrContinues", category = "Design")
@Messages({
  "DN_TooManyBreakOrContinues=Break or Continue count",
  "DESC_TooManyBreakOrContinues=Indicates there are more break or continue statements inside one loop than specified."
})
public class TooManyBreakOrContinues {

  @IntegerOption(defaultValue = 3, step = 1, displayName = "Threshold", tooltip = "Maximum amount of break and continue statements.")
  private static final String THRESHOLD = "Threshold";

  private static final Collection<Kind> CONSIDERED_KINDS = EnumSet.of(Kind.FOR_LOOP, Kind.WHILE_LOOP, Kind.ENHANCED_FOR_LOOP, Kind.DO_WHILE_LOOP);

  @TriggerTreeKind({Kind.FOR_LOOP, Kind.WHILE_LOOP, Kind.ENHANCED_FOR_LOOP, Kind.DO_WHILE_LOOP})
  @Messages({
    "# {0} - present1",
    "# {1} - allowed1",
    "ERR_TooManyBreakOrContinues=Found {0} break/continue statements. Desired is <= {1}"})
  public static ErrorDescription computeWarning(HintContext ctx) {
    ErrorDescription ret = null;

    final int threshhold = ctx.getPreferences().getInt(THRESHOLD, 3);

    final TreePath path = ctx.getPath();

    if (CONSIDERED_KINDS.contains(path.getLeaf().getKind())) {
      final StatementTree methodTree = (StatementTree) path.getLeaf();

      // have one visitor per loop
      final TooManyOfCounterVisitor tooManyOfCounterVisitor = new TooManyOfCounterVisitor();
      tooManyOfCounterVisitor.scan(methodTree, 0);

      final int scan = tooManyOfCounterVisitor.getBreaksAndConts();

      if (scan > threshhold) {
        ret = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_TooManyBreakOrContinues(scan, threshhold));
      }

    }

    return ret;
  }

}
