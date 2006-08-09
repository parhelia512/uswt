package org.eclipse.swt.tools.internal;

import java.io.PrintStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

import org.eclipse.swt.SWT;

public abstract class CNIGenerator {

  private static final Hashtable casts = new Hashtable();

  static {
    casts.put(new ParameterKey("_XCheckIfEvent", 2),
              "(int (*)(Display*, XEvent*, char*))");
  }

  private static int compare(Class a, Class b) {
    return a.getName().compareTo(b.getName());
  }

  private static void sort(Class[] classes) {
    Arrays.sort(classes, new Comparator() {
        public int compare(Object a, Object b) {
          return CNIGenerator.compare((Class) a, (Class) b);
        }
      });
  }

  private static String name(Class c) {
    String name = c.getName();
    return name.substring(name.lastIndexOf('.') + 1, name.length());
  }

  private static String name(Method m) {
    String name = m.getName();
    if (name.startsWith("_")) name = name.substring(1);
    return name;
  }

  private static String accessor(Class c, FieldData data) {
    String accessor = data.getAccessor();
    if (name(c).equals("XVisualInfo") && accessor.equals("class"))
      accessor = "c_class";
    return accessor;
  }

  private static void generateStructureFunctionDeclarations
    (PrintStream out, Class[] classes, MetaData metaData)
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (metaData.getMetaData(classes[i]).getGenerate()) {
        out.print("#ifndef NO_");
        out.println(name(classes[i]));

        generateReaderDeclaration(out, classes[i]);
        generateWriterDeclaration(out, classes[i]);

        out.println("#endif");
      }
    }

    out.println();    
  }

  private static PrintStream headerOut(String prefix)
    throws Exception
  {
    return new PrintStream(new BufferedOutputStream
                           (new FileOutputStream(prefix + "swt.h")));
  }

  private static PrintStream structureFunctionOut(String prefix, Class c)
    throws Exception
  {    
    PrintStream out = new PrintStream(new BufferedOutputStream
                                      (new FileOutputStream
                                       (prefix + name(c) + "-structs.cpp")));
    out.println("#include \"swt.h\"");
    out.println();
    return out;
  }

  private static PrintStream nativeOut(String prefix, Class c)
    throws Exception
  {    
    PrintStream out = new PrintStream(new BufferedOutputStream
                                      (new FileOutputStream
                                       (prefix + name(c) + "-natives.cpp")));
    out.println("#include \"swt.h\"");
    out.println();
    return out;
  }

  private static void generateStructureFunctions(String prefix,
                                                 Class[] classes,
                                                 MetaData metaData)
    throws Exception
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (metaData.getMetaData(classes[i]).getGenerate()) {
        PrintStream out = structureFunctionOut(prefix, classes[i]);
        try {
          out.print("#ifndef NO_");
          out.println(name(classes[i]));

          generateReader(out, classes[i], metaData);
          out.println();

          generateWriter(out, classes[i], metaData);

          out.println("#endif");
          out.println();
        } finally {
          out.close();
        }
      }
    }
  }

  private static boolean ignoreField(Field field) {
    int m = field.getModifiers();
    return
      ((m & Modifier.PUBLIC) == 0) ||
      ((m & Modifier.FINAL) != 0) ||
      ((m & Modifier.STATIC) != 0);
  }

  private static void generateReaderFields(PrintStream out, Class c,
                                           MetaData metaData)
  {
    Class superClass = c.getSuperclass();
    String name = name(c);
    String superName = name(superClass);
    if (superClass != Object.class) {
      // Windows exception - cannot call get/set function of super
      // class in this case.
      if (! (name.equals(superName + "A") || name.equals(superName + "W"))) {
        out.print("  get");
        out.print(superName);
        out.print("Fields(src, (");
        out.print(superName);
        out.println("*) dst);");
      } else {
        generateReaderFields(out, superClass, metaData);
      }
    }

    Field[] fields = c.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      if (ignoreField(fields[i])) continue;

      FieldData data = metaData.getMetaData(fields[i]);

      String exclude = data.getExclude();
      if (exclude.length() > 0) out.println(exclude);

      Class type = fields[i].getType();
      String accessor = accessor(c, data);

      if (accessor == null || accessor.length() == 0)
        accessor = fields[i].getName();

      if (type.isPrimitive()) {
        out.print("  dst->");
        out.print(accessor);
        out.print(" = ");
        out.print("(typeof(");
        out.print("dst->");
        out.print(accessor);
        out.print("))");
        out.print(" src->");
        out.print(fields[i].getName());
        out.println(";");
      } else if (type.isArray()) {
        Class componentType = type.getComponentType();
        if (componentType.isPrimitive()) {
          out.print("  for (unsigned i = 0; i < src->");
          out.print(fields[i].getName());
          out.println("->length; ++i) {");
        
          out.print("    dst->");
          out.print(accessor);
          out.print("[i] = elements(*(src->");
          out.print(fields[i].getName());
          out.println("))[i];");

          out.println("  }");
        } else {
          throw new RuntimeException("not yet implemented");
        }
      } else {
        String typeName = name(type);

        out.print("  if (src->");
        out.print(fields[i].getName());
        out.print(") get");
        out.print(typeName);
        out.print("Fields(src->");
        out.print(fields[i].getName());
        out.print(", &(dst->");
        out.print(accessor);
        out.println("));");
      }
    }
  }

  private static void generateReaderPrototype(PrintStream out, Class c) {
    String name = name(c);

    out.print("void get");
    out.print(name);
    out.print("Fields(");
    generateTypeName(out, c);
    out.print("* src, ");
    out.print(name);
    out.print("* dst)");
  }  

  private static void generateReaderDeclaration(PrintStream out, Class c) {
    generateReaderPrototype(out, c);
    out.println(";");
  }

  private static void generateReader(PrintStream out, Class c,
                                     MetaData metaData)
  {
    generateReaderPrototype(out, c);
    out.println();

    out.println("{");

    generateReaderFields(out, c, metaData);

    out.println("}");
  }
  
  private static int byteCount(Class c) {
    if (c == Integer.TYPE) return 4;
    if (c == Boolean.TYPE) return 4;
    if (c == Long.TYPE) return 8;
    if (c == Short.TYPE) return 2;
    if (c == Character.TYPE) return 2;
    if (c == Byte.TYPE) return 1;
    if (c == Float.TYPE) return 4;
    if (c == Double.TYPE) return 8;
    throw new RuntimeException
      ("can't determine byte count for " + c.getName());
  }

  private static void generateWriterFields(PrintStream out, Class c,
                                           MetaData metaData)
  {
    Class superClass = c.getSuperclass();
    String name = name(c);
    String superName = name(superClass);
    if (superClass != Object.class) {
      // Windows exception - cannot call get/set function of super
      // class in this case.
      if (! (name.equals(superName + "A") || name.equals(superName + "W"))) {
        out.print("  set");
        out.print(superName);
        out.print("Fields(dst, (");
        out.print(superName);
        out.println("*) src);");
      } else {
        generateWriterFields(out, superClass, metaData);
      }
    }

    Field[] fields = c.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      if (ignoreField(fields[i])) continue;

      FieldData data = metaData.getMetaData(fields[i]);

      String exclude = data.getExclude();
      if (exclude.length() > 0) out.println(exclude);

      Class type = fields[i].getType();
      String accessor = accessor(c, data);

      if (accessor == null || accessor.length() == 0)
        accessor = fields[i].getName();

      if (type.isPrimitive()) {
        out.print("  dst->");
        out.print(fields[i].getName());
        out.print(" = (");
        generateType(out, type);
        out.print(") src->");
        out.print(accessor);
        out.println(";");
      } else if (type.isArray()) {
        Class componentType = type.getComponentType();
        if (componentType.isPrimitive()) {
          out.print("  for (unsigned i = 0; i < sizeof(src->");
          out.print(fields[i].getName());
          out.print(")");

          int byteCount = byteCount(componentType);
          if (byteCount > 1) {
            out.print(" / ");
            out.print(String.valueOf(byteCount));
          }

          out.println("; ++i) {");
        
          out.print("    elements(*(dst->");
          out.print(fields[i].getName());
          out.print("))[i] = src->");
          out.print(accessor);
          out.println("[i];");

          out.println("  }");
        } else {
          throw new RuntimeException("not yet implemented");
        }
      } else {
        String typeName = name(type);

        out.print("  if (dst->");
        out.print(fields[i].getName());
        out.print(") set");
        out.print(typeName);
        out.print("Fields(&(dst->");
        out.print(fields[i].getName());
        out.print("), src->");
        out.print(accessor);
        out.println(");");
      }
    }
  }

  private static void generateWriterPrototype(PrintStream out, Class c) {
    String name = name(c);

    out.print("void set");
    out.print(name);
    out.print("Fields(");
    generateTypeName(out, c);
    out.print("* dst, ");
    out.print(name);
    out.print("* src)");
  }

  private static void generateWriterDeclaration(PrintStream out, Class c) {
    generateWriterPrototype(out, c);
    out.println(";");
  }
  
  private static void generateWriter(PrintStream out, Class c,
                                     MetaData metaData)
  {
    generateWriterPrototype(out, c);
    out.println();
    
    out.println("{");

    generateWriterFields(out, c, metaData);

    out.println("}");
  }
  
  private static void generateNatives(String prefix, Class[] classes,
                                      MetaData metaData)
    throws Exception
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (metaData.getMetaData(classes[i]).getGenerate()) {
        PrintStream out = nativeOut(prefix, classes[i]);
        try {
          generateMethods(out, classes[i], metaData);
        } finally {
          out.close();
        }
      }
    }
  }

  private static void generateIncludes(PrintStream out, Class[] classes,
                                       MetaData metaData)
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      generateInclude(out, classes[i]);
    }

    out.println();
  }

  private static void generateInclude(PrintStream out, Class c) {
    out.print("#include \"");
    for (StringTokenizer st = new StringTokenizer(c.getName(), ".");
         st.hasMoreTokens();)
    {
      out.print(st.nextToken());
      if (st.hasMoreTokens()) out.print("/");
    }
    out.println(".h\"");
  }

  private static int compareParameters(Method a, Method b) {
    Class[] pa = a.getParameterTypes();
    Class[] pb = b.getParameterTypes();
    if (pa.length == pb.length) {
      for (int i = 0; i < pa.length; ++i) {
        int r = compare(pa[i], pb[i]);
        if (r != 0) return r;
      }
      return 0;
    } else {
      return pa.length - pb.length;
    }
  }

  private static void sort(Method[] methods) {
    Arrays.sort(methods, new Comparator() {
        public int compare(Object a, Object b) {
          Method ma = (Method) a;
          Method mb = (Method) b;
          int r = ma.getName().compareTo(mb.getName());
          return (r == 0 ? compareParameters(ma, mb) : r);
        }
      });
  }
  
  private static void generateMethods(PrintStream out, Class c,
                                      MetaData metaData)
  {
    Method[] methods = c.getDeclaredMethods();
    sort(methods);

    for (int i = 0; i < methods.length; ++i) {
      if ((methods[i].getModifiers() & Modifier.NATIVE) != 0 &&
          metaData.getMetaData(methods[i]).getGenerate())
      {
        out.print("#ifndef NO_");
        out.println(name(methods[i]));

        generateMethod(out, methods[i], true, metaData);
        out.println();

        generateMethod(out, methods[i], false, metaData);

        out.println("#endif");
        out.println();
      }
    }
  }

  private static void generateTypeName(PrintStream out, Class c) {
    for (StringTokenizer st = new StringTokenizer(c.getName(), ".");
         st.hasMoreTokens();)
    {
      String token = st.nextToken();
      if (! st.hasMoreTokens()) out.print("MacroProtect_");
      out.print(token);
      if (st.hasMoreTokens()) out.print("::");
    }
  }

  private static void generateType(PrintStream out, Class c) {
    if (c == Void.TYPE) out.print("void");
    else if (c == Integer.TYPE) out.print("jint");
    else if (c == Boolean.TYPE) out.print("jboolean");
    else if (c == Long.TYPE) out.print("jlong");
    else if (c == Short.TYPE) out.print("jshort");
    else if (c == Character.TYPE) out.print("jchar");
    else if (c == Byte.TYPE) out.print("jbyte");
    else if (c == Float.TYPE) out.print("jfloat");
    else if (c == Double.TYPE) out.print("jdouble");
    else if (c.isArray()) {
      out.print("JArray< ");
      generateType(out, c.getComponentType());
      out.print(" >*");
    } else {
      generateTypeName(out, c);
      out.print("*");
    }
  }

  private static void generateType3(PrintStream out, Class c, boolean struct) {
    if (c == Void.TYPE) out.print("void");
    else if (c == Integer.TYPE) out.print("int32_t");
    else if (c == Boolean.TYPE) out.print("bool");
    else if (c == Long.TYPE) out.print("int64_t");
    else if (c == Short.TYPE) out.print("int16_t");
    else if (c == Character.TYPE) out.print("int16_t");
    else if (c == Byte.TYPE) out.print("int8_t");
    else if (c == Float.TYPE) out.print("float");
    else if (c == Double.TYPE) out.print("double");
    else if (c.isArray()) {
      generateType3(out, c.getComponentType(), true);
      if (! struct) out.print("*");
    } else {
      out.print(name(c));
      if (! struct) out.print("*");
    }
  }

  private static void generateType3(PrintStream out, Class c) {
    generateType3(out, c, false);
  }

  private static boolean isSystemClass(Class c) {
    return c == Object.class || c == Class.class;
  }

  private static boolean generateLocals(PrintStream out, Method m,
                                        MetaData metaData)
  {
    boolean needsReturn = false;
    Class[] types = m.getParameterTypes();
    for (int i = 0; i < types.length; ++i) {
      if (types[i].isPrimitive() || isSystemClass(types[i])) continue;
      ParameterData data = metaData.getMetaData(m, i);
      if (types[i] == String.class) {
        out.print("  ");
        if (data.getFlag("unicode")) {
          out.print("const jchar* pp");
          out.print(i);
          out.print(" = 0;");
        } else {
          out.print("int al");
          out.print(i);
          out.print(" = JvGetStringUTFLength(p");
          out.print(i);
          out.println(");");

          out.print("const char pp");
          out.print(i);
          out.print("[pl");
          out.print(i);
          out.println(") + 1];");
        }
      } else if (! types[i].isArray()) {
        out.print("  ");
        out.print(name(types[i]));
        out.print(" ps");
        out.print(i);
        if (data.getFlag("init")) out.print(" = { 0 }");
        out.println(";");
      }
      
      needsReturn |= (! (types[i].isPrimitive() || isSystemClass(types[i]) ||
                         types[i] == String.class || types[i].isArray() ||
                         data.getFlag("no_out")));
    }

    if (needsReturn && m.getReturnType() != Void.TYPE) {
      out.print("  ");
      generateType(out, m.getReturnType());
      out.println(" rc = 0;");
    }

    return needsReturn;
  }

  private static void generateRead(PrintStream out, Method m, Class type,
                                   ParameterData data, int i)
  {
    if (type.isPrimitive() || isSystemClass(type) || type.isArray()) return;

    if (type == String.class) {
      if (data.getFlag("unicode")) {
        out.print("  pp");
        out.print(i);
        out.print(" = JvGetStringChars(p");
        out.print(i);
        out.println(");");
      } else {
        out.print("  JvGetStringChars(p");
        out.print(i);
        out.print(", 0, pl");
        out.print(i);
        out.print(", pp");
        out.print(i);
        out.println(");");
      }
    } else if (! data.getFlag("no_in")) {
      out.print("  if (p");
      out.print(i);
      out.print(") get");
      out.print(name(type));
      out.print("Fields(p");
      out.print(i);
      out.print(", &ps");
      out.print(i);
      out.println(");");
    }
  }

  private static void generateReads(PrintStream out, Method m,
                                    MetaData metaData)
  {
    Class[] types = m.getParameterTypes();
    for (int i = 0; i < types.length; ++i) {
      generateRead(out, m, types[i], metaData.getMetaData(m, i), i);
    }
  }

  private static void generateWrite(PrintStream out, Method m, Class type,
                                    ParameterData data, int i)
  {
    if (type.isPrimitive() || isSystemClass(type) || type == String.class
        || type.isArray() || data.getFlag("no_out")) return;

    out.print("  if (p");
    out.print(i);
    out.print(") set");
    out.print(name(type));
    out.print("Fields(p");
    out.print(i);
    out.print(", &ps");
    out.print(i);
    out.println(");");
  }

  private static void generateWrites(PrintStream out, Method m,
                                     MetaData metaData)
  {
    Class[] types = m.getParameterTypes();
    for (int i = types.length - 1; i >= 0; --i) {
      generateWrite(out, m, types[i], metaData.getMetaData(m, i), i);
    }
  }

  private static void generateCallLeftSide(PrintStream out, Method m,
                                           MethodData data,
                                           boolean needsReturn)
  {
    out.print("  ");
    if (m.getReturnType() != Void.TYPE) {
      if (needsReturn) {
        out.print("rc = (");
      } else {
        out.print("return (");
      }
      generateType(out, m.getReturnType());
      out.print(") ");
    }
    if (data.getFlag("address")) {
      out.print("&");
    }
  }

  private static String cast(Method m, int index, ParameterData data) {
    String cast = (String) casts.get(new ParameterKey(m, index));
    return (cast == null ? data.getCast() : cast);
  }

  private static void generateCallRightSide(PrintStream out, Method m,
                                            MethodData data, int paramStart,
                                            MetaData metaData)
  {
    if (! data.getFlag("const")) {
      out.print("(");
      Class[] types = m.getParameterTypes();
      for (int i = paramStart; i < types.length; ++i) {
        ParameterData pdata = metaData.getMetaData(m, i);
        if (pdata.getFlag("struct")) out.print("*");
        String cast = cast(m, i, pdata);
        if (cast.length() > 2) {
          out.print(cast);
          out.print(" ");
        }
        if (types[i].isPrimitive() || isSystemClass(types[i])) {
          if (cast.length() <= 2) {
            out.print("(");
            generateType3(out, types[i], true);
            out.print(") ");
          }
          out.print("p");
          out.print(i);
        } else if (types[i] == String.class) {
          out.print("pp");
          out.print(i);
        } else if (types[i].isArray()) {
          if (cast.length() <= 2) {
            out.print("(");
            generateType3(out, types[i], false);
            out.print(") ");
          }
          out.print("(p");
          out.print(i);
          out.print(" ? elements(p");
          out.print(i);
          out.print(") : 0)");
        } else {
          out.print("&ps");
          out.print(i);
        }
        if (i < types.length - 1) out.print(", ");
      }
      out.print(")");
    }
    out.print(";");
  }

  private static void generateProcedureTypedef(PrintStream out, Method m,
                                               MetaData metaData)
  {
    out.print("    typedef ");
    generateType(out, m.getReturnType());
    out.print(" (*Procedure)(");

    Class[] types = m.getParameterTypes();
    for (int i = 0; i < types.length; i++) {
      ParameterData pdata = metaData.getMetaData(m, i);
      String cast = cast(m, i, pdata);
      if (cast.length() > 2) {
        out.print(cast.substring(1, cast.length() - 1));
      } else {
        generateType3(out, types[i], pdata.getFlag("struct"));
      }
      if (i < types.length - 1) out.print(", ");
    }

    out.println(");");
  }

  private static void generateDynamicCall(PrintStream out, Method m,
                                          MethodData data, boolean needsReturn,
                                          MetaData metaData)
  {
    out.println("  {");
    
    String name = name(m);

    if (SWT.getPlatform().equals("win32")) {
      out.println("    static bool initialized = false;");
      out.println("    static HMODULE module = 0;");
      generateProcedureTypedef(out, m, metaData);
      out.println("    static Procedure procedure = 0;");
      out.println("    if (not initialized) {");
      out.print("      if (module == 0) module = LoadLibrary(");
      out.print(name);
      out.println("_LIB);");
      out.print("      if (module) procedure = (Procedure) ");
      out.print("GetProcAddress(module, \"");
      out.print(name);
      out.println("\");");
      out.println("      initialized = true;");
      out.println("    }");
      out.println("    if (procedure) {");
      out.print("    ");
      generateCallLeftSide(out, m, data, needsReturn);
      out.print("procedure");
      generateCallRightSide(out, m, data, 0, metaData);
      out.println();
      out.println("    }");
    } else if (SWT.getPlatform().equals("carbon")) {
      out.println("    static bool initialized = false;");
      out.println("    static CFBundleRef bundle = 0;");
      generateProcedureTypedef(out, m, metaData);
      out.println("    static Procedure procedure;");
      out.println("    if (not initialized) {");
      out.print("      if (bundle == 0) bundle = ");
      out.print("CFBundleGetBundleWithIdentifier(CFSTR(");
      out.print(name);
      out.println("_LIB));");
      out.print("      if (bundle) procedure = (Procedure) ");
      out.print("CFBundleGetFunctionPointerForName(bundle, CFSTR(\"");
      out.print(name);
      out.println("\"));");
      out.println("      initialized = true;");
      out.println("    }");
      out.println("    if (procedure) {");
      out.print("    ");
      generateCallLeftSide(out, m, data, needsReturn);
      out.print("(*procedure)");
      generateCallRightSide(out, m, data, 0, metaData);
      out.println();
      out.println("    }");
    } else {
      out.println("    static bool initialized = false;");
      out.println("    static void* handle = 0;");
      out.print("    typedef ");
      generateType(out, m.getReturnType());
      generateProcedureTypedef(out, m, metaData);
      out.println("    static Procedure procedure;");
      if (m.getReturnType() != Void.TYPE) {
        if (needsReturn) {
          out.println("    rc = 0;");
        }
      }
      out.println("    if (not initialized) {");
      out.print("      if (handle == 0) handle = dlopen(");
      out.print(name);
      out.println("_LIB, RTLD_LAZY);");
      out.print("      if (handle) procedure = (Procedure) dlsym(handle, \"");
      out.print(name);
      out.println("\");");
      out.println("      initialized = true;");
      out.println("    }");
      out.println("    if (procedure) {");
      out.print("    ");
      generateCallLeftSide(out, m, data, needsReturn);
      out.print("(*procedure)");
      generateCallRightSide(out, m, data, 0, metaData);
      out.println();
      out.println("    }");
    }

    out.println("  }");
  }

  private static boolean mightBeMacro(Method m, MethodData data) {
    String name = name(m);
    return name.endsWith("_sizeof");
  }

  private static void generateCall(PrintStream out, Method m, MethodData data,
                                   boolean needsReturn, boolean asMacro,
                                   MetaData metaData)
  {
    String copy = (String) data.getParam("copy");
    boolean makeCopy = copy.length() != 0 && m.getReturnType() != Void.TYPE;
    if (makeCopy) {
      out.print("  ");
      out.print(copy);
      out.print(" temp = ");
    } else {
      generateCallLeftSide(out, m, data, needsReturn);
    }

    int paramStart = 0;
    String name = name(m);

    if (name.equalsIgnoreCase("call")) {
      out.print("(");
      ParameterData pdata = metaData.getMetaData(m, 0);
      String cast = cast(m, 0, pdata);
      if (cast.length() > 2) {
        out.print(cast);
      } else {
        out.print("(");
        generateType(out, m.getReturnType());
        out.print(" (*)(...))");
      }
      out.print(" p0)");
      paramStart = 1;
    } else if (name.startsWith("VtblCall")) {
      out.print("((");
      generateType3(out, m.getReturnType());
      out.print(" (STDMETHODCALLTYPE*)("); 

      Class[] types = m.getParameterTypes();
      for (int i = 1; i < types.length; i++) {
        generateType3(out, types[i]);
        if (i < types.length - 1) out.print(", ");
      }

      out.print("))(*(");
      generateType3(out, types[1]);
      out.print("**) p1)[p0])");
      paramStart = 1;
    } else if (data.getFlag("cpp")) {
      out.print("(");
      ParameterData pdata = metaData.getMetaData(m, 0);
      if (pdata.getFlag("struct")) out.print("*");
      String cast = cast(m, 0, pdata);
      if (cast.length() > 2) {
        out.print(cast);
        out.print(" ");
      }
      out.print("p0)->");
      String accessor = data.getAccessor();
      if (accessor.length() != 0) {
        out.print(accessor);
      } else {
        int index = name.indexOf('_');
        if (index != -1) {
          out.print(name.substring(index + 1));
        } else {
          out.print(name);
        }
      }
      paramStart = 1;
    } else if (data.getFlag("new")) {
      out.print("new ");
      String accessor = data.getAccessor();
      if (accessor.length() != 0) {
        out.print(accessor);
      } else {
        int index = name.indexOf('_');
        if (index != -1) {
          out.print(name.substring(index + 1));
        } else {
          out.print(name);
        }
      }      
    } else if (data.getFlag("delete")) {
      out.print("delete ");
      ParameterData pdata = metaData.getMetaData(m, 0);
      String cast = cast(m, 0, pdata);
      if (cast.length() > 2) {
        out.print(cast);
      } else {
        out.print("(");
        out.print(name.substring(0, name.indexOf("_")));
        out.print("*)");
      }
      out.println(" p0;");
      return;
    } else if (name.endsWith("_sizeof")) {
      String n = name.substring(0, name.lastIndexOf("_sizeof"));
      if (asMacro) {
        out.print(n);
        out.println("_sizeof();");
      } else {
        out.print("sizeof(");
        out.print(n);
        out.println(");");
      }
      return;
    } else {
      String accessor = data.getAccessor();
      if (accessor.length() != 0) {
        out.print(accessor);
      } else {
        out.print(name);
      }
    }
    
    generateCallRightSide(out, m, data, paramStart, metaData);
    out.println();

    if (makeCopy) {
      out.println("  {");

      out.print("    ");
      out.print(copy);
      out.print("* copy = new ");
      out.print(copy);
      out.println("();");

      out.println("    *copy = temp;");

      out.print("    rc = (");
      generateType(out, m.getReturnType());
      out.println(") copy;");

      out.println("  }");
    }
  }

  private static void generateReturn(PrintStream out, Method m,
                                     boolean needsReturn)
  {
    if (needsReturn && m.getReturnType() != Void.TYPE) {
      out.println("  return rc;");
    }
  }
  
  private static void generateGTKmemmove(PrintStream out, Method m) {
    Class[] types = m.getParameterTypes();
    if (types[0].isPrimitive()) {
      String className = name(types[1]);
      out.print("  if (p1) get");
      out.print(className);
      out.print("Fields(p1, (::");
      out.print(className);
      out.println("*) p0);");
    } else {
      String className = name(types[0]);
      out.print("  if (p0) set");
      out.print(className);
      out.print("Fields(p0, (::");
      out.print(className);
      out.print("*) p1);");
    }
  }

  private static void generateMethod(PrintStream out, Method m,
                                     boolean generateInline,
                                     MetaData metaData)
  {
    if (generateInline) {
      out.print("inline ");
    }

    generateType(out, m.getReturnType());
    out.println();

    if (generateInline) {
      out.print("inline_");
    } else {
      generateTypeName(out, m.getDeclaringClass());
      out.println("::");
      out.print("MacroProtect_");
    }

    out.print(m.getName());
    out.println();

    out.print("  (");    
    Class[] parameters = m.getParameterTypes();
    for (int i = 0; i < parameters.length; ++i) {
      generateType(out, parameters[i]);
      out.print(" p");
      out.print(i);
      if (i < parameters.length - 1) out.print(", ");
    }
    out.println(")");
    
    out.println("{");

    MethodData data = metaData.getMetaData(m);

    String name = name(m);
    Class[] types = m.getParameterTypes();

    if (generateInline) {
      boolean isGTKmemove = name.equals("memmove") && types.length == 2 &&
        m.getReturnType() == Void.TYPE;
      if (isGTKmemove) {
        generateGTKmemmove(out, m);
      } else {
        boolean needsReturn = generateLocals(out, m, metaData);
        generateReads(out, m, metaData);
        if (data.getFlag("dynamic")) {
          generateDynamicCall(out, m, data, needsReturn, metaData);
        } else if (mightBeMacro(m, data)) {
          out.print("#ifdef ");
          out.println(name(m));
          generateCall(out, m, data, needsReturn, true, metaData);
          out.println("#else");
          generateCall(out, m, data, needsReturn, false, metaData);
          out.println("#endif");
        } else {
          generateCall(out, m, data, needsReturn, false, metaData);
        }
        generateWrites(out, m, metaData);
        generateReturn(out, m, needsReturn);
      }
    } else {
      out.print("  return inline_");
      out.print(m.getName());
      out.print("(");  
      for (int i = 0; i < parameters.length; ++i) {
        out.print("p");
        out.print(i);
        if (i < parameters.length - 1) out.print(", ");
      }
      out.println(");");
    }

    out.println("}");
  }

  private static void generateAll(String prefix) throws Exception {
    PrintStream out = headerOut(prefix);
    MyGeneratorApp app = new MyGeneratorApp(out, prefix);
    try {
      System.out.println("first stage:");
      app.stage = 1;
      app.generateAll();
      if (SWT.getPlatform().equals("gtk")) {
        out.println("#include \"cairo_custom.h\"");
        out.println("#include \"cairo.h\"");
        out.println("#include \"cairo-xlib.h\"");
        out.println("#include \"glx.h\"");
      } else if (SWT.getPlatform().equals("win32")) {
        out.println("#include \"windows.h\"");
        out.println("#include \"docobj.h\"");
        out.println("#include \"commctrl.h\"");
        out.println("#include \"gdiplus.h\"");
        out.println("#include \"stdint.h\"");
        out.println("#include \"com_custom.h\"");
      }
      out.println("#include \"os.h\"");
      out.println();

      out.println("#undef TRUE");
      out.println("#define TRUE 1");
      out.println();

      System.out.println("\nsecond stage:");
      app.stage = 2;
      app.generateAll();
    } finally {
      out.close();
    }

    System.out.println("\nthird stage:");
    app.stage = 3;
    app.generateAll();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println
        ("usage: java " + CNIGenerator.class.getName() +
         " <output_prefix>");
      System.exit(-1);
    }

    generateAll(args[0]);
  }

  private static class MyGeneratorApp extends JNIGeneratorApp {
    public final PrintStream headerOut;
    public final String prefix;
    public int stage = 1;
    
    private MyGeneratorApp(PrintStream headerOut, String prefix) {
      this.headerOut = headerOut;
      this.prefix = prefix;
    }

    public void generate() {
      if (getMainClassName().contains(".cde.") ||
          getMainClassName().contains(".gnome.") ||
          getMainClassName().contains(".mozilla."))
      {
        return;
      }

      switch (stage) {
      case 1:
        generateIncludes(headerOut, getStructureClasses(), getMetaData());
        generateIncludes(headerOut, getNativesClasses(), getMetaData());
        break;

      case 2:
        generateStructureFunctionDeclarations(headerOut, getStructureClasses(),
                                              getMetaData());
        break;

      case 3:
        generateStructureFunctions(prefix, getStructureClasses(),
                                   getMetaData());
        generateNatives(prefix, getNativesClasses(), getMetaData());
        break;

      default: throw new RuntimeException("unexpected stage: " + stage);
      }
    }

    String getMetaDataDir() {
      return "org.eclipse.swt.tools/JNI Generation/org/eclipse/swt/tools/internal/";
    }
  }

  private static class ParameterKey {
    private final String methodName;
    private final int index;

    private ParameterKey(String methodName, int index) {
      this.methodName = methodName;
      this.index = index;
    }

    private ParameterKey(Method m, int index) {
      this(m.getName(), index);
    }

    public int hashCode() {
      return methodName.hashCode();
    }

    public boolean equals(Object o) {
      if (o instanceof ParameterKey) {
        ParameterKey pk = (ParameterKey) o;
        return pk.methodName.equals(methodName) && pk.index == index;
      } else {
        return false;
      }
    }
  }

}
