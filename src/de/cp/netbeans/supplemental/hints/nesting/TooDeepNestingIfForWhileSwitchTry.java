package de.cp.netbeans.supplemental.hints.nesting;


import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;

@Hint(displayName = "#DN_TooDeepNestingIfForWhileSwitchTry", description = "#DESC_TooDeepNestingIfForWhileSwitchTry", category = "Design")
@Messages({
  "DN_TooDeepNestingIfForWhileSwitchTry=Nesting of If/For/While/Switch/Try",
  "DESC_TooDeepNestingIfForWhileSwitchTry=Checks the control flow statements \"if\", \"for\", \"while\", \"switch\" and \"try\" are not be nested too deeply."
})
public class TooDeepNestingIfForWhileSwitchTry {

  @IntegerOption(defaultValue = 3, step = 1, displayName = "Threshold", tooltip = "Maximum depth of nesting of \"if\", \"for\", \"while\", \"switch\" and \"try\" statements.")
  private static final String THRESHOLD = "Threshold";

  @TriggerTreeKind({Kind.METHOD})
  @Messages({
    "# {0} - present1",
    "ERR_TooDeepNestingIfForWhileSwitchTry=Control flow statements nested to deeply (more than {0})."})
  public static ErrorDescription computeWarning(HintContext ctx) {
    ErrorDescription ret = null;

    final int threshhold = ctx.getPreferences().getInt(THRESHOLD, 3);

    final TreePath path = ctx.getPath();

    if (Kind.METHOD == path.getLeaf().getKind()) {
      final Tree methodTree = path.getLeaf();

      // have one visitor per loop
      final NestedIfStatementsVisitor visitor = new NestedIfStatementsVisitor(threshhold);
      visitor.scan(methodTree, 0);

      final Tree reachingTree = visitor.getReachingTree();

      if (reachingTree != null) {
        ret = ErrorDescriptionFactory.forName(ctx, reachingTree, Bundle.ERR_TooDeepNestingIfForWhileSwitchTry(threshhold));
      }

    }

    return ret;
  }

}
