# FileRename

## About

Aim of this project is to provide a mapping solution between the various image naming patterns of mobile device and
camera manufacturers. Since most manufacturers do not support defining a custom file naming pattern, having several
different devices can lead to a multitude of file names for events in close temporal proximity.

The aims of this application are twofold:
* Firstly, the application should provide a command line based solution for this issue, especially with regards to
different timestamp formats. This solution is intended to be run automatically, e.g. after importing images from a device
or as cron job.
* Secondly, the application should allow extending and manipulating the ruleset for such transformations easily.

## How To Use

```
Usage: filerename [-dhrV] -i=<inputTemplate> -o=<outputTemplate> [-p=<path>]
  -d, --dryRun        Setting this parameter will only display how the file
                        names will be changed
  -h, --help          Show this help message and exit.
  -i, --input=<inputTemplate>
                      The pattern of the input file names
  -o, --output=<outputTemplate>
                      The pattern of the output file names
  -p, --path=<path>   The directory for the operation
  -r, --recursive     Include sub directories.
  -V, --version       Print version information and exit.
```

### Available Templates/Transformation Rules

#### Regex Transformation Rule - `<<R|{regular_expression}`

This rule allows to define a regular expression against which the filenames are matched.

This can be used in two ways: 
* Either only as part as the input pattern to match several files. Subsequently, other rules can be defined in the output pattern to modify these files:

| InputPattern | OutputPattern   | Filename      | Resulting Filename |
|-----------|-----------------|---------------|--------------------|
| `IMAGE_<<R | [0-9]{3}>>.jpg` | `picture-<<CD |yyyy-MM-dd>>.JPG` | `IMAGE001.jpg` | `picture-2020-10-21.JPG` | 

* As part of both input and output pattern: This allows to shift a defined char sequence in many files following the same pattern to a different position if the output pattern.
  | InputPattern | OutputPattern   | Filename      | Resulting Filename |
  |-----------|-----------------|---------------|--------------------|
  | `IMAGE_<<R | [0-9]{3}>>.jpg` | `<<R>>-picture.JPG` | `IMAGE001.jpg` | `001-picture.JPG` |


#### Timestamp Transformation Rule - `<<TS|{timestamp_pattern>>`

This rule allows defining source and target timestamp patterns based on
the [Java API patterns for formatting and parsing date and time information](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)
.

##### Example:

```
filerename -i "img<<TS|ddMMyyyyHHmmssSSS>>.jpg" -o "image<<TS|yyyy-MM-dd_HHmmssSSS>>.jpg" 
```

The arguments specified above will cause the following behavior:

| Input Filename             | Output Filename                            |
|----------------------------|--------------------------------------------|
| `img20122021125401451.jpg` | `image2021-12-20_125401451.jpg`            |
| `img4112202101023456.jpg`  | Failure due to incorrect date information  |
| `img.jpg`                  | Does not match pattern and won't be treated |

#### File Creation Date Transformation Rule - `<<CD|{timestamp_pattern}}`
If no timestamp is available as part of the filename, the file creation date can be used instead. 

If no `timestamp_pattern` is provided, `ISO_DATE_TIME` will be used per default. 

##### Example:

```
filerename -i "image.jpg" -o "image<<CD|yyyy-MM-dd_HHmmSS>>.jpg" 
```

The arguments specified above will cause the following behavior:

| Input Filename | Output Filename              |
|----------------|------------------------------|
| `image.jpg`    | `image2021-12-20_125401.jpg` |

#### Enumeration Transformation Rule - `<<E|{format_string_syntax}}`

This rule allows to add increasing numbers to the filename. Additional formatting arguments can be provided using
the  [Java Format String syntax](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Formatter.html#syntax)
. If no formatting argument is provided (`<<E>>`) only the number itself will be used (synonymous with `<<E|%d>>`).

##### Example:

```
filerename -i "image.jpg" -o "image<<E|%02d>>.jpg" 
```

The arguments specified above will cause the following behavior:

| Input Filename | Argument | Output Filename |
|----------------|----------|-----------------|
| `image.jpg`    | `%d`     | `image1.jpg`    |
| `image.jpg`    | `%02d`   | `image01.jpg`   |

## How to Extend the Ruleset

New rules/patterns can be added easily:

1. You can implement the logic for the transformation rules using two approaches:
    1. Add a new transformation rule by implementing all required methods of the `TransformationRule` interface
    2. Add a new transformation rule by extending the `AbstractTransformationRule` class. This class provides several
       utility functions that allow you to focus on implementing the actual logic.
2. Add the generator function to the `TransformationRuleFactoryConfiguration` in the form of
   a `TransformationRuleGenerator` that consumes the input and output pattern provided as String and provides
   an `Optional<TransformationRule>` as result. Please take the contract of the API into consideration to ensure proper
   functionality:
    * return `Optional.empty()` if the pattern provided does not indicate that the rule is applicable to the task
    * return `Optional.of(yourRuleInstance)` in cases where the rule is applicable to the task
    * throw an `IllegalArgumentException` in cases where the input and/or output string does clearly indicate wrong
      input that can't be parsed (e.g. `image<<ddMMyyyy.jpg` for a timestamp transformation rule)

### Some side notes:

* Rules will be applied in parallel. Ensure sufficient atomicity where needed.
* Rules will be created once per task and hence share a common state which allows for the creation of rules that e.g.
  depend on the number of other invocations.
