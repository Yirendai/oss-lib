package com.yirendai.oss.lib.test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.net.URLClassLoader;

import org.junit.Test;

/**
 * see: http://stackoverflow.com/questions/42102/using-different-classloaders-for-different-junit-tests
 * see: http://stackoverflow.com/questions/24431427/multiple-runwith-statements-in-junit
 * see: https://github.com/BinaryTweed/quarantining-test-runner
 */
public class ApplicationInSeparateClassLoaderTest {

  @Test
  public void testApplicationInSeparateClassLoader1() throws Exception {
    testApplicationInSeparateClassLoader();
  }

  @Test
  public void testApplicationInSeparateClassLoader2() throws Exception {
    testApplicationInSeparateClassLoader();
  }

  private void testApplicationInSeparateClassLoader() throws Exception {
    //run application code in separate class loader in order to isolate static state between test runs
    final Runnable runnable = mock(Runnable.class);
    //set up your mock object expectations here, if needed
    InterfaceToApplicationDependentCode tester = makeCodeToRunInSeparateClassLoader(
      "com.yirendai", InterfaceToApplicationDependentCode.class, CodeToRunInApplicationClassLoader.class);
    //if you want to try the code without class loader isolation, comment out above line and comment in the line below
    //CodeToRunInApplicationClassLoader tester = new CodeToRunInApplicationClassLoaderImpl();
    tester.testTheCode(runnable);
    verify(runnable).run();
    assertEquals("should be one invocation!", 1, tester.getNumOfInvocations());
  }

  /**
   * Create a new class loader for loading application-dependent code and return an instance of that.
   */
  @SuppressWarnings("unchecked")
  private <I, T> I makeCodeToRunInSeparateClassLoader(
    String packageName, Class<I> testCodeInterfaceClass, Class<T> testCodeImplClass) throws Exception {
    TestApplicationClassLoader cl = new TestApplicationClassLoader(
      packageName, getClass(), testCodeInterfaceClass);
    Class<?> testerClass = cl.loadClass(testCodeImplClass.getName());
    return (I) testerClass.newInstance();
  }

  /**
   * Bridge interface, implemented by code that should be run in application class loader.
   * This interface is loaded by the same class loader as the unit test class, so
   * we can call the application-dependent code without need for reflection.
   */
  public interface InterfaceToApplicationDependentCode {
    void testTheCode(Runnable run);
    int getNumOfInvocations();
  }

  /**
   * Test-specific code to call application-dependent code. This class is loaded by
   * the same class loader as the application code.
   */
  public static class CodeToRunInApplicationClassLoader implements InterfaceToApplicationDependentCode {
    private static int numOfInvocations = 0;

    @Override
    public void testTheCode(Runnable runnable) {
      numOfInvocations++;
      runnable.run();
    }

    @Override
    public int getNumOfInvocations() {
      return numOfInvocations;
    }
  }

  /**
   * Loads application classes in separate class loader from test classes.
   */
  private static class TestApplicationClassLoader extends URLClassLoader {

    private final String appPackage;
    private final String mainTestClassName;
    private final String[] testSupportClassNames;

    public TestApplicationClassLoader(String appPackage, Class<?> mainTestClass, Class<?>... testSupportClasses) {
      super(((URLClassLoader) getSystemClassLoader()).getURLs());
      this.appPackage = appPackage;
      this.mainTestClassName = mainTestClass.getName();
      this.testSupportClassNames = convertClassesToStrings(testSupportClasses);
    }

    private String[] convertClassesToStrings(Class<?>[] classes) {
      String[] results = new String[classes.length];
      for (int i = 0; i < classes.length; i++) {
        results[i] = classes[i].getName();
      }
      return results;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
      if (isApplicationClass(className)) {
        //look for class only in local class loader
        return super.findClass(className);
      }
      //look for class in parent class loader first and only then in local class loader
      return super.loadClass(className);
    }

    private boolean isApplicationClass(String className) {
      if (mainTestClassName.equals(className)) {
        return false;
      }
      for (int i = 0; i < testSupportClassNames.length; i++) {
        if (testSupportClassNames[i].equals(className)) {
          return false;
        }
      }
      return className.startsWith(appPackage);
    }

  }

}
