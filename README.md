# netbeans-java-hints
Provides additional hints for java in the NetBeans IDE. These hints are related to complexity and readability.

## Available hints
### Cognitive Complexity
This type of complexity measurement was introduced first by [SonarSource](https://www.sonarsource.com/docs/CognitiveComplexity.pdf). 
As good as McCabe's complexity measurement is, Sonarsouce's cognitive complexity suits much better the modern languages.

The plugin allows you to 
* set the threshold when to display a warning
* choose whether to display the complexity always on a method
* show the reason why the complexity is increased at a certain point
* show by how much a code location increments the complexity of the method

### Nesting of statements
This hints checks whether the control flow statements `if`, `for`, `while`, `switch` and `try` are not nested too deep into each other.
You can set a threshold when this issue should be raised. Default is 3.

**Example**
We assume the default threshold of 3 and this code:
```
if (i < 3) {
  if (i < 4) {
    if (i < 5) {
      if (i < 6) {
      }
    }
  }
}
```
In line ``if (i < 6)`` a hint will appear indicating the nesting is too deep.

### Statement counts
For readability not too many statements of a certain type should be used in code blocks. Although this is a matter of personal preferences, many teams might have clear rules about it.
These two rules helps to see violations of these rules.
##### Too many return statemens
Counts the return statements inside a method and if a specified threshold is exceeded (default is 3) an issue is raised.
##### Too many break/continue statements
Counts the ``break`` **and** ``continue`` statements inside a method and if a specified threshold is exceeded (default is 3) an issues is raised.
Please note, that these two statements are counted together, so if you have e.g. one break statement and three continues, this rule will apply and raise the issue.

