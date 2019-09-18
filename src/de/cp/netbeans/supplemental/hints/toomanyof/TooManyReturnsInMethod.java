package de.cp.netbeans.supplemental.hints.toomanyof;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_TooManyReturnsInMethod", description = "#DESC_TooManyReturnsInMethod", category = "Design", severity = Severity.WARNING)
@Messages({
  "DN_TooManyReturnsInMethod=Return statement count",
  "DESC_TooManyReturnsInMethod=Indicates there are too many return statements in a method."
})
public class TooManyReturnsInMethod {

  @IntegerOption(defaultValue = 3, step = 1, displayName = "Threshold", tooltip = "Maximum amount of desired return statements.")
  private static final String THRESHOLD = "Threshold";

  @Messages({
      "# {0} - present",
      "# {1} - allowed",
      "ERR_TooManyReturnsInMethod=Found {0} return statements. Desired is <= {1}"})
  @TriggerTreeKind(Tree.Kind.METHOD)
  public static ErrorDescription computeWarning(HintContext ctx) {
    ErrorDescription ret = null;

    final int threshhold = ctx.getPreferences().getInt(THRESHOLD, 3);

    final TreePath path = ctx.getPath();

    if(Tree.Kind.METHOD == path.getLeaf().getKind()) {
      final MethodTree methodTree = (MethodTree)path.getLeaf();

      final TooManyOfCounterVisitor tooManyOfCounterVisitor = new TooManyOfCounterVisitor();
      tooManyOfCounterVisitor.scan(methodTree, 0);

      final int scan = tooManyOfCounterVisitor.getReturns();

      if(scan > threshhold) {
        ret = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_TooManyReturnsInMethod(scan, threshhold));
      }

    }

    return ret;
  }

}
