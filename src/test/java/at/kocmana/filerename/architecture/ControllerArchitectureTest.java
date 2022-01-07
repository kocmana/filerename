package at.kocmana.filerename.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ControllerArchitectureTest {

  private static final JavaClasses importedClasses = new ClassFileImporter().importPackages("at.kocmana.filerename");

  @Test
  void onlyControllerClassesMayRelyOnTheCliDependency(){
    ArchRule rule = noClasses().that().haveNameNotMatching(".*Controller.*")
            .should().dependOnClassesThat().resideInAPackage("picocli")
            .because("Service classes should not depend on implementation specifics of the controller.");

    rule.check(importedClasses);
  }

}
