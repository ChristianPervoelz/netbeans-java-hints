/*
 *  Copyright 2017 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: cpervoelz
 *
 *  $Id$
 */
package de.cp.netbeans.supplemental.hints.complexity;

import com.sun.source.tree.LineMap;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.BooleanOption;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.IntegerOption;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 * Hint implementation to show the coginitve complexity as described in https://www.sonarsource.com/docs/CognitiveComplexity.pdf.
 *
 * @version 0.2
 * @author cperv
 * @since 1.0
 */
@Hint(displayName = "#DN_TooHighCognitiveMethodComplexity", description = "#DESC_TooHighCognitiveMethodComplexity", category = "Design", severity = Severity.WARNING)
@NbBundle.Messages({
  "DN_TooHighCognitiveMethodComplexity=Cognitive complexity",
  "DESC_TooHighCognitiveMethodComplexity=Checks a methods cognitive complexity does not exceed a given threshold (default is 15).<p>This implementation is oriented on the"
  + "SonarQube way. Their definition can be found here: "
  + "<a href=\"https://www.sonarsource.com/docs/CognitiveComplexity.pdf\">https://www.sonarsource.com/docs/CognitiveComplexity.pdf</a></p>"
})
public class CognitiveComplexityHint {

  @IntegerOption(defaultValue = 15, step = 1, displayName = "Threshold", tooltip = "Minimum cognitive complexity to be reached before the hint is shown.")
  private static final String THRESHOLD = "Threshold";

  @BooleanOption(defaultValue = false, displayName = "Show complexity always", tooltip = "When checked the complexity is always shown in the hints.")
  private static final String SHOW_ALWAYS = "Show Always";

  @BooleanOption(defaultValue = false, displayName = "Show increments", tooltip = "When checked the increments added are shown at the line of the according statement as a hint.")
  private static final String SHOW_INCREMENTS = "Show increments";

  @BooleanOption(defaultValue = false, displayName = "Show reason for increments", tooltip = "When checked the reason for an increment is shown.")
  private static final String SHOW_REASONS = "Show Reasons";

  @NbBundle.Messages({
    "# {0} - found",
    "# {1} - 15 (default)",
    "ERR_TooHighCognitiveMethodComplexity=Cognitive complexity is {0}. Desired is <= {1}."})
  @TriggerTreeKind(Tree.Kind.METHOD)
  public static Collection<ErrorDescription> computeWarning(final HintContext ctx) {
    final Collection<ErrorDescription> ret = new HashSet<>();

    final TreePath path = ctx.getPath();

    if (Tree.Kind.METHOD == path.getLeaf().getKind()) {
      final MethodTree methodTree = (MethodTree) path.getLeaf();
      final Preferences preferences = ctx.getPreferences();

      CognitiveComplexityVisitor visitor = new CognitiveComplexityVisitor(preferences.getBoolean(SHOW_REASONS, false));

      methodTree.accept(visitor, 0);

      int complexity = visitor.getComplexity();

      final int threshhold = preferences.getInt(THRESHOLD, 15);
      final boolean showIncrements = preferences.getBoolean(SHOW_INCREMENTS, false);
      final boolean showAlways = preferences.getBoolean(SHOW_ALWAYS, false);

      if (complexity > threshhold || showAlways) {
        // add the general message
        ret.add(ErrorDescriptionFactory.forName(ctx, ctx.getPath(), Bundle.ERR_TooHighCognitiveMethodComplexity(complexity, threshhold)));

        if (showIncrements) {
          // if we show the increments, find the tree in our results and add it to the result set
          ret.addAll(createMessagesForStatements(ctx, visitor.getPenalties()));
        }
      }
    }

    return ret;
  }

  private static Collection<ErrorDescription> createMessagesForStatements(final HintContext context, final Map<Tree, String> penalties) {
    final Collection<ErrorDescription> ret = new HashSet<>();

    final Map<Long, LineInfo> mapping = new HashMap<>();

    for (final Entry<Tree, String> entry : penalties.entrySet()) {
      final LineInfo lineInfo = getLineInfo(context, entry.getKey(), entry.getValue());
      if(lineInfo != null && lineInfo.line > 0) {
        LineInfo mapped = mapping.get(lineInfo.line);
        if(mapped == null) {
          mapping.put(lineInfo.line, lineInfo);
        } else {
          mapped.infos.addAll(lineInfo.infos);
        }
      }
    }

    for (final LineInfo value : mapping.values()) {
      if(value.start > -1) {
        String text = value.infos.stream().collect(Collectors.joining(" "));
        ret.add(org.netbeans.spi.editor.hints.ErrorDescriptionFactory.createErrorDescription(Severity.HINT, text, new ArrayList<>(), context.getInfo().getFileObject(), value.start, value.start));
      }
    }

    return ret;
  }

  private static LineInfo getLineInfo(final HintContext context, final Tree tree, final String message) {
    LineInfo ret = null;
    int start = (int) context.getInfo().getTrees().getSourcePositions().getStartPosition(context.getInfo().getCompilationUnit(), tree);
    int javacEnd = (int) context.getInfo().getTrees().getSourcePositions().getEndPosition(context.getInfo().getCompilationUnit(), tree);
    int end = Math.min(javacEnd, findLineEnd(context.getInfo(), start));

    if (start != -1 && end != -1) {
      final LineMap lineMap = context.getInfo().getCompilationUnit().getLineMap();
      long startline = lineMap.getLineNumber(start);
      if(startline > 0) {
        ret = new LineInfo();
        ret.start = start;
        ret.line = startline;
        ret.infos.add(message);
      }
    }

    return ret;
  }

  private static int findLineEnd(CompilationInfo info, int start) {
    String text = info.getText();

    for (int i = start + 1; i < text.length(); i++) {
      if (text.charAt(i) == '\n') {
        return i;
      }
    }

    return text.length();
  }

  private static final class LineInfo {
    int start = -1;
    long line = 0L;
    Collection<String> infos = new ArrayList<>();
  }

}
