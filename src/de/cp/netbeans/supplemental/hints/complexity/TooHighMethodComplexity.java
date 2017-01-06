/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cp.netbeans.supplemental.hints.complexity;

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.prefs.Preferences;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle.Messages;

@Hint(displayName = "#DN_TooHighMethodComplexity", description = "#DESC_TooHighMethodComplexity", category = "Design", severity = Severity.WARNING, enabled = false )
@Messages({
  "DN_TooHighMethodComplexity=McCabe's cyclomatic method complexity",
  "DESC_TooHighMethodComplexity=Checks a methods complexity does not exceed a given threshold (default is 10)."
      + "<p>By default this hint is always disabled, as <ul><li>the cognitive complexity is a much better indicator, and</li><li>the results of this are incorrect sometimes"
      + "</li></ul>.</p>"
})
public class TooHighMethodComplexity {

  @IntegerOption(defaultValue = 10, step = 1, displayName = "Threshold", tooltip = "Minimum complexity to be reached before the hint is shown.")
  private static final String THRESHOLD = "Threshold";

  @BooleanOption(defaultValue = false, displayName = "Show complexity always", tooltip = "When checked the complexity is always shown in the hints.")
  private static final String SHOW_ALWAYS = "Show Always";

  @BooleanOption(defaultValue = true, displayName = "Count \"throw\" statements", tooltip = "Adds a complexity point if a throw statment is encountered.")
  private static final String COUNT_THROW = "Count Throw";

  @Messages({
      "# {0} - found",
      "# {1} - 10 (default)",
      "ERR_TooHighMethodComplexity=Methods complexity is {0}. Desired is <= {1}."})
  @TriggerTreeKind(Tree.Kind.METHOD)
  public static ErrorDescription computeWarning(final HintContext ctx) {

    ErrorDescription ret = null;

    final TreePath path = ctx.getPath();

    if(Tree.Kind.METHOD == path.getLeaf().getKind()) {
      final MethodTree methodTree = (MethodTree)path.getLeaf();
      final CyclomaticComplexityVisitor visitor = new CyclomaticComplexityVisitor();

      Integer scan = visitor.scan(methodTree, 0);

      final Preferences preferences = ctx.getPreferences();

      final int threshhold = preferences.getInt(THRESHOLD, 10);
      final boolean showAlways = preferences.getBoolean(SHOW_ALWAYS, false);

      if(!preferences.getBoolean(COUNT_THROW, true)) {
        scan -= visitor.getThrows();
      }

      if(scan != null && (scan > threshhold || showAlways)) {
        ret = ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_TooHighMethodComplexity(scan, threshhold));
      }
    }

    return ret;
  }
}
