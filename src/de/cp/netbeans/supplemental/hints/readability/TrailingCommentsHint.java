/*
 *  Copyright 2017 ADVA Optical Networking SE. All rights reserved.
 *
 *  Owner: cpervoelz
 *
 *  $Id$
 */

package de.cp.netbeans.supplemental.hints.readability;

import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import java.util.List;
import org.netbeans.api.java.source.Comment;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.ErrorDescriptionFactory;
import org.netbeans.spi.java.hints.Hint;
import org.netbeans.spi.java.hints.HintContext;
import org.netbeans.spi.java.hints.TriggerTreeKind;
import org.openide.util.NbBundle;

/**
 *
 * @version
 * @author cperv
 * @since
 */
@Hint(displayName = "#DN_TrailingComments", description = "#DESC_TrailingComments", category = "Design", severity = Severity.WARNING)
@NbBundle.Messages({
  "DN_TrailingComments=Trailing comments",
  "DESC_TrailingComments=Checks whether there are trailing comments"
})
public class TrailingCommentsHint {

  @TriggerTreeKind(Tree.Kind.CLASS)
  public static ErrorDescription computeWarning(final HintContext ctx) {

    List<Comment> comments = ctx.getInfo().getTreeUtilities().getComments(ctx.getPath().getLeaf(), true);
    List<Comment> comments1 = ctx.getInfo().getTreeUtilities().getComments(ctx.getPath().getLeaf(), false);

//    System.out.println(comments);
//    System.out.println(comments1);
//    System.out.println("");

    return null;
  }

}
