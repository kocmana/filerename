# FileRename

## About

Aim of this project is to provide a mapping solution between the various image naming patterns of mobile device and
camera manufacturers. Since most manufacturers do not support defining a custom file naming pattern, having several
different devices can lead to a multitude of file names for events in close temporal proximity.

The aim of this application is to provide a command line based solution for this issue, especially with regards to
different timestamp formats. This solution is inteded to be run automatically, e.g. after importing images from a device
or as cron job.

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

#### Timestamp Transformation Rule - `<<{timestamp_pattern>>`

This rule allows defining source and target timestamp patterns based on
the [Java API patterns for formatting and parsing date and time information](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html).

##### Example:

```
filerename -i "img<<ddMMyyyyHHmmssSSS>>.jpg" -o "image<<yyyy-MM-dd_HHmmssSSS>>.jpg" 
```

The arguments specified above will cause the following behavior:

| Input Filename             | Output Filename                             |
|----------------------------|---------------------------------------------|
| `img20122021125401451.jpg` | `image2021-12-20_125401451.jpg`             |
| `img4112202101023456.jpg`  | Failure due to incorrect date information   |
| `img.jpg`                  | Does not match pattern and won't be treated |


## How to Extend the Ruleset

New rules/patterns can be added easily: 
1. Add a new transformation rule by implementing all required methods of the `TransformationRule` interface
2. Add the generator function to the `TransformationRuleFactoryConfiguration` in the form of a `TransformationRuleGenerator` that consumes the input and output pattern provided as String and provides an `Optional<TransformationRule` as result. Please take the contract of the API into consideration to ensure proper functionality: 
   * return `Optional.empty()` if the pattern provided does not indicate that the rule is applicable to the task
   * return `Optional.of(yourRuleInstance)` in cases where the rule is applicable to the task
   * throw an `IllegalArgumentException` in cases where the input and/or output string does clearly indicate wrong input that can't be parsed (e.g. `image<<ddMMyyyy.jpg` for a timestamp transformation rule)

### Some side notes: 
* Rules will be applied in parallel. Ensure sufficient atomicity where needed.
* Rules will be created once per task and hence share a common state which allows for the creation of rules that e.g. depend on the number of other invocations.