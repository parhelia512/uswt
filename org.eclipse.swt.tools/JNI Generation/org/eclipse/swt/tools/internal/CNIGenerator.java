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
  private static final boolean TRACE_FUNCTIONS = false;

  private static final Hashtable casts = new Hashtable();
  private static final Hashtable fieldArrayLengths = new Hashtable();

  private static final int NORMAL_STYLE = 1;
  private static final int INLINE_STYLE = 2;
  private static final int PROXY_STYLE = 3;
  private static final int PROXY_CALL_STYLE = 4;

  static {
    casts.put(new ParameterKey("_XCheckIfEvent", 2),
              "(int (*)(Display*, XEvent*, char*))");
    casts.put(new ParameterKey("CGDataProviderCreateWithData", 3),
              "(void (*)(void*, const void*, size_t))");
    
    fieldArrayLengths.put("org.eclipse.swt.internal.gdip.ColorPalette.Entries",
                          new Integer(1));
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

  private static void generateProxy(PrintStream out, Class c,
                                    MetaData metaData)
  {
    out.print("extern \"C\" struct CNIProxy_");
    out.print(name(c));
    out.println(" {");

    generateProxyFields(out, c, metaData);

    out.println("};");
  }

  private static void generateProxyFields(PrintStream out, Class c,
                                          MetaData metaData)
  {
    Class superClass = c.getSuperclass();
    String name = name(c);
    String superName = name(superClass);
    if (superClass != Object.class) {
      generateProxyFields(out, superClass, metaData);
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

      out.print("  ");
      Integer arrayLength = (Integer) fieldArrayLengths.get
        (c.getName() + "." + fields[i].getName());
      generateType3(out, type, arrayLength != null);
      out.print(" ");
      out.print(accessor);
      if (arrayLength != null) {
        out.print("[");
        out.print(arrayLength);
        out.print("]");
      }
      out.println(";");

      if (exclude.length() > 0) out.println("#endif");
    }    
  }  

  private static PrintStream getOut(String name) throws Exception {
    return new PrintStream(new BufferedOutputStream
                           (new FileOutputStream(name)));  
  }

  private static PrintStream nativeOut(String prefix)
    throws Exception
  {
    return getOut(prefix + "swt.cpp");
  }

  private static PrintStream foreignOut(String prefix)
    throws Exception
  {
    return getOut(prefix + "swt-foreign.cpp");
  }

  private static PrintStream foreignDefOut(String prefix)
    throws Exception
  {
    PrintStream out = getOut(prefix + "swt-foreign.def");
    out.println("LIBRARY SWT-FOREIGN.DLL");
    out.println("EXPORTS");
    return out;
  }

  private static PrintStream headerOut(String prefix, int style)
    throws Exception
  {
    PrintStream out = getOut(prefix + (style == PROXY_STYLE ?
                                       "swt-foreign.h" : "swt.h"));
    out.println("#ifndef SWT_H");
    out.println("#define SWT_H");
    return out;
  }

  private static PrintStream proxyHeaderOut(String prefix, Class c)
    throws Exception
  {
    PrintStream out = getOut(prefix + name(c) + "-proxy.h");

    out.print("#ifndef ");
    out.print(name(c));
    out.println("_PROXY_H");

    out.print("#define ");
    out.print(name(c));
    out.println("_PROXY_H");
    return out;
  }

  private static PrintStream structureHeaderOut(String prefix, Class c,
                                                int style)
    throws Exception
  {
    PrintStream out = getOut(prefix + name(c) + (style == PROXY_STYLE ?
                                                 "-foreign-structs.h" :
                                                 "-structs.h"));

    out.print("#ifndef ");
    out.print(name(c));
    out.println("_STRUCTS_H");

    out.print("#define ");
    out.print(name(c));
    out.println("_STRUCTS_H");

    out.println(style == PROXY_STYLE ?
                "#include \"swt-foreign.h\"" : "#include \"swt.h\"");
    out.println();
    return out;
  }

  private static PrintStream structureFunctionOut(String prefix, Class c,
                                                  int style)
    throws Exception
  {
    PrintStream out = getOut(prefix + name(c) + (style == PROXY_STYLE ?
                                                 "-foreign-structs.cpp" :
                                                 "-structs.cpp"));

    out.print("#include \"");
    out.print(name(c));
    out.print(style == PROXY_STYLE ? "-foreign-structs.h" : "-structs.h");
    out.println("\"");
    out.println();
    return out;
  }

  private static PrintStream nativeOut(String prefix, Class c, int style)
    throws Exception
  {
    PrintStream out = getOut(prefix + name(c) + (style == PROXY_STYLE ?
                                                 "-foreign-natives.cpp" :
                                                 "-natives.cpp"));
    out.println(style == PROXY_STYLE ?
                "#include \"swt-foreign.h\"" : "#include \"swt.h\"");
    out.println();
    return out;
  }

  private static void generateStructureHeader(PrintStream out, Class c,
                                              int style, MetaData metaData,
                                              boolean include)
  {
    out.print("#ifndef NO_");
    out.println(name(c));

    if (include && (style == PROXY_STYLE || style == PROXY_CALL_STYLE)) {
      out.print("#include \"");
      out.print(name(c));
      out.println("-proxy.h\"");
      out.println();
    }

    generateReaderDeclaration(out, c, style);
    generateWriterDeclaration(out, c, style);

    out.println("#endif");
  }

  private static void generateStructureHeaders(PrintStream nativeOut,
                                               PrintStream foreignOut,
                                               String prefix, Class[] classes,
                                               MetaData metaData)
    throws Exception
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      ClassData data = metaData.getMetaData(classes[i]);
      if (data.getGenerate()) {
        if (incompatibleABI(classes[i])) {
          if (nativeOut == null) {
            PrintStream out = proxyHeaderOut(prefix, classes[i]);
            try {
              generateProxy(out, classes[i], metaData);
              out.println("#endif");
            } finally {
              out.close();
            }

            out = structureHeaderOut(prefix, classes[i], PROXY_STYLE);
            try {
              generateStructureHeader(out, classes[i], PROXY_STYLE, metaData,
                                      true);
              out.println("#endif");
            } finally {
              out.close();
            }
          } else {
            generateProxy(nativeOut, classes[i], metaData);
            generateProxy(foreignOut, classes[i], metaData);

            generateStructureHeader(foreignOut, classes[i], PROXY_STYLE,
                                    metaData, false);
          }
        }

        if (nativeOut == null) {
          PrintStream out = structureHeaderOut(prefix, classes[i],
                                               NORMAL_STYLE);
          try {
            generateStructureHeader(out, classes[i],
                                    (incompatibleABI(classes[i]) ?
                                     PROXY_CALL_STYLE : NORMAL_STYLE),
                                    metaData, true);
            out.println("#endif");
          } finally {
            out.close();
          }
        } else {
          generateStructureHeader(nativeOut, classes[i],
                                  (incompatibleABI(classes[i]) ?
                                   PROXY_CALL_STYLE : NORMAL_STYLE),
                                  metaData, false);
        }
      }
    }
  }

  private static void generateStructureFunctions(PrintStream out, Class c,
                                                 int style, MetaData metaData)
    throws Exception
  {
    out.print("#ifndef NO_");
    out.println(name(c));

    generateReader(out, c, style, metaData);
    out.println();

    generateWriter(out, c, style, metaData);

    out.println("#endif");
    out.println();
  }

  private static void generateStructureFunctions(PrintStream nativeOut,
                                                 PrintStream foreignOut,
                                                 String prefix,
                                                 Class[] classes,
                                                 MetaData metaData)
    throws Exception
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (metaData.getMetaData(classes[i]).getGenerate()) {
        if (incompatibleABI(classes[i])) {
          if (nativeOut == null) {
            PrintStream out = structureFunctionOut(prefix, classes[i],
                                                   PROXY_STYLE);
            try {
              generateStructureFunctions(out, classes[i], PROXY_STYLE,
                                         metaData);
            } finally {
              out.close();
            }
          } else {
            generateStructureFunctions(foreignOut, classes[i], PROXY_STYLE,
                                       metaData);            
          }
        }

        int style = (incompatibleABI(classes[i]) ?
                     PROXY_CALL_STYLE : NORMAL_STYLE);
        if (nativeOut == null) {
          PrintStream out = structureFunctionOut(prefix, classes[i],
                                                 NORMAL_STYLE);
          try {
            generateStructureFunctions(out, classes[i], style, metaData);
          } finally {
            out.close();
          }
        } else {
          generateStructureFunctions(nativeOut, classes[i], style, metaData);
        }
      }
    }
  }

  private static boolean incompatibleABI(Class c) {
    return c.getName().startsWith("org.eclipse.swt.internal.gdip.");
  }

  private static boolean ignoreField(Field field) {
    int m = field.getModifiers();
    return
      ((m & Modifier.PUBLIC) == 0) ||
      ((m & Modifier.FINAL) != 0) ||
      ((m & Modifier.STATIC) != 0);
  }

  private static void generateReaderFields(PrintStream out, Class c, int style,
                                           MetaData metaData)
  {
    Class superClass = c.getSuperclass();
    String name = name(c);
    String superName = name(superClass);
    if (superClass != Object.class) {
      // Windows exception - cannot call get/set function of super
      // class in this case.
      if (! (name.equals(superName + "A") || name.equals(superName + "W"))) {
        if (style == PROXY_STYLE) {
          out.print("  CNIProxy_get");
        } else {
          out.print("  get");
        }
        out.print(superName);
        out.print("Fields(src, (");
        out.print(superName);
        out.println("*) dst);");
      } else {
        generateReaderFields(out, superClass, style, metaData);
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

      if (style == PROXY_STYLE) {
        if (type.isPrimitive()) {
          out.print("  dst->");
          out.print(accessor);
          out.print(" = ");
          out.print(data.getCast());
          out.print(" src->");
          out.print(accessor);
          out.println(";");
        } else if (type.isArray()) {
          generateProxyArrayCopy(out, fields[i], accessor);
        } else {
          throw new RuntimeException("not implemented");
        }
      } else {
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
            int index = accessor.indexOf("[");
            if (index > 0) {
              out.print("  dst->");
              out.print(accessor);
              out.print(" = elements(*(src->");
              out.print(fields[i].getName());
              out.print("))");
              out.print(accessor.substring(index));
              out.println(";");
            } else {
              out.print("  for (unsigned i = 0; i < src->");
              out.print(fields[i].getName());
              out.println("->length; ++i) {");
        
              out.print("    dst->");
              out.print(accessor);
              out.print("[i] = elements(*(src->");
              out.print(fields[i].getName());
              out.println("))[i];");

              out.println("  }");
            }
          } else {
            throw new RuntimeException("not implemented");
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

      if (exclude.length() > 0) out.println("#endif");
    }
  }

  private static void generateReaderPrototype(PrintStream out, Class c,
                                              int style)
  {
    String name = name(c);

    if (style == PROXY_STYLE) {
      out.print("void CNIProxy_get");
    } else {
      out.print("void get");
    }
    out.print(name);
    out.print("Fields(");
    if (style == PROXY_STYLE) {
      out.print("CNIProxy_");
      out.print(name);
    } else {
      generateTypeName(out, c);
    }
    out.print("* src, ");
    if (style == PROXY_CALL_STYLE) {
      out.print("CNIProxy_");
    }
    out.print(name);
    out.print("* dst)");
  }  

  private static void generateReaderDeclaration(PrintStream out, Class c,
                                                int style)
  {
    generateReaderPrototype(out, c, style);
    out.println(";");
  }

  private static void generateReader(PrintStream out, Class c, int style,
                                     MetaData metaData)
  {
    String name = name(c);
    generateReaderPrototype(out, c, style);
    out.println();

    out.println("{");

    generateReaderFields(out, c, style, metaData);

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

  private static void generateProxyArrayCopy(PrintStream out, Field f,
                                             String accessor)
  {
    Class componentType = f.getType().getComponentType();
    if (componentType.isPrimitive()) {
      out.print("  for (unsigned i = 0; i < sizeof(src->");
      out.print(accessor);
      out.print(")");
            
      int byteCount = byteCount(componentType);
      if (byteCount > 1) {
        out.print(" / ");
        out.print(String.valueOf(byteCount));
      }

      out.println("; ++i) {");
            
      out.print("    dst->");
      out.print(accessor);
      out.print("[i] = src->");
      out.print(accessor);
      out.println("[i];");
            
      out.println("  }");
    } else {
      throw new RuntimeException("not implemented");
    }
  }

  private static void generateWriterFields(PrintStream out, Class c, int style,
                                           MetaData metaData)
  {
    Class superClass = c.getSuperclass();
    String name = name(c);
    String superName = name(superClass);
    if (superClass != Object.class) {
      // Windows exception - cannot call get/set function of super
      // class in this case.
      if (! (name.equals(superName + "A") || name.equals(superName + "W"))) {
        if (style == PROXY_STYLE)
          out.print("  CNIProxy_set");
        else
          out.print("  set");
        out.print(superName);
        out.print("Fields(dst, (");
        out.print(superName);
        out.println("*) src);");
      } else {
        generateWriterFields(out, superClass, style, metaData);
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

      if (style == PROXY_STYLE) {
        if (type.isPrimitive()) {
          out.print("  dst->");
          out.print(accessor);
          out.print(" = (");
          generateType3(out, type);
          out.print(") src->");
          out.print(accessor);
          out.println(";");
        } else if (type.isArray()) {
          generateProxyArrayCopy(out, fields[i], accessor);
        } else {
          throw new RuntimeException("not implemented");
        }
      } else {
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
            int index = accessor.indexOf("[");
            if (index > 0) {
              out.print("  elements(*(dst->");
              out.print(fields[i].getName());
              out.print("))");
              out.print(accessor.substring(index));
              out.print(" = src->");
              out.print(accessor);
              out.println(";");
            } else {
              out.print("  for (unsigned i = 0; i < sizeof(src->");
              out.print(accessor);
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
            }
          } else {
            throw new RuntimeException("not implemented");
          }
        } else {
          String typeName = name(type);

          out.print("  if (dst->");
          out.print(fields[i].getName());
          out.print(") set");
          out.print(typeName);
          out.print("Fields(dst->");
          out.print(fields[i].getName());
          out.print(", &(src->");
          out.print(accessor);
          out.println("));");
        }
      }
      
      if (exclude.length() > 0) out.println("#endif");
    }
  }

  private static void generateWriterPrototype(PrintStream out, Class c,
                                              int style)
  {
    String name = name(c);

    if (style == PROXY_STYLE) {
      out.print("void CNIProxy_set");
    } else {
      out.print("void set");
    }
    out.print(name);
    out.print("Fields(");
    if (style == PROXY_STYLE) {
      out.print("CNIProxy_");
      out.print(name);
    } else {
      generateTypeName(out, c);
    }
    out.print("* dst, ");
    if (style == PROXY_CALL_STYLE) {
      out.print("CNIProxy_");
    }
    out.print(name);
    out.print("* src)");
  }

  private static void generateWriterDeclaration(PrintStream out, Class c,
                                                int style)
  {
    generateWriterPrototype(out, c, style);
    out.println(";");
  }
  
  private static void generateWriter(PrintStream out, Class c, int style,
                                     MetaData metaData)
  {
    String name = name(c);
    generateWriterPrototype(out, c, style);
    out.println();
    
    out.println("{");

    generateWriterFields(out, c, style, metaData);

    out.println("}");
  }
  
  private static void generateNatives(PrintStream nativeOut,
                                      PrintStream foreignOut,
                                      String prefix, Class[] classes,
                                      MetaData metaData)
    throws Exception
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (metaData.getMetaData(classes[i]).getGenerate()) {
        if (incompatibleABI(classes[i])) {
          if (nativeOut == null) {
            PrintStream out = nativeOut(prefix, classes[i], PROXY_STYLE);
            try {
              generateMethods(out, classes[i], PROXY_STYLE, metaData);
            } finally {
              out.close();
            }
          } else {
            generateMethods(foreignOut, classes[i], PROXY_STYLE, metaData);
          }
        }

        int style = (incompatibleABI(classes[i]) ?
                     PROXY_CALL_STYLE : NORMAL_STYLE);
        if (nativeOut == null) {
          PrintStream out = nativeOut(prefix, classes[i], style);
          try {
            generateMethods(out, classes[i], style, metaData);
          } finally {
            out.close();
          }
        } else {
          generateMethods(nativeOut, classes[i], style, metaData);
        }
      }
    }
  }

  private static void generateNativeDefs(PrintStream out, Class[] classes,
                                         MetaData metaData)
    throws Exception
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (metaData.getMetaData(classes[i]).getGenerate()) {
        if (incompatibleABI(classes[i])) {
          generateNativeDefs(out, classes[i]);
        }
      }
    }
  }

  private static void generateNativeDefs(PrintStream out, Class c)
    throws Exception
  {
    Method[] methods = c.getDeclaredMethods();
    sort(methods);

    for (int i = 0; i < methods.length; ++i) {
      if ((methods[i].getModifiers() & Modifier.NATIVE) != 0) {
        out.print("CNIProxy_");
        out.print(methods[i].getName());
        generateDecoration(out, methods[i]);
//         out.print("@");
//         int count = 0;
//         Class[] parameters = methods[i].getParameterTypes();
//         for (int j = 0; j < parameters.length; ++j) {
//           if (parameters[j].isPrimitive()) {
//             count += byteCount(parameters[j]);
//           } else {
//             // assume 32-bit pointers since this code should only be
//             // run on win32:
//             count += 4;
//           }
//         }
//         out.println(String.valueOf(count));
        out.println();
      }
    }
  }

  private static void generateStructureFunctionIncludes(PrintStream out,
                                                        Class[] classes,
                                                        MetaData metaData)
  {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (metaData.getMetaData(classes[i]).getGenerate()) {
        out.print("#include \"");
        out.print(name(classes[i]));
        out.println("-structs.h\"");
      }
    }

    out.println();
  }

  private static void generateIncludes(PrintStream out, Class[] classes) {
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

  private static void generateProxyIncludes(PrintStream out, Class[] classes) {
    sort(classes);

    for (int i = 0; i < classes.length; ++i) {
      if (incompatibleABI(classes[i])) {
        generateProxyInclude(out, classes[i]);
      }
    }

    out.println();
  }

  private static void generateProxyInclude(PrintStream out, Class c) {
    out.print("#include \"");
    out.print(name(c));
    out.println("-foreign-structs.h\"");
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
  
  private static void generateMethods(PrintStream out, Class c, int style,
                                      MetaData metaData)
  {
    Method[] methods = c.getDeclaredMethods();
    sort(methods);

    for (int i = 0; i < methods.length; ++i) {
      if ((methods[i].getModifiers() & Modifier.NATIVE) != 0 &&
          (metaData.getMetaData(methods[i]).getGenerate() ||
           style == PROXY_CALL_STYLE))
      {
        // for debugging:
        if (style == PROXY_CALL_STYLE) {
          generateMethodPrototype(out, methods[i], NORMAL_STYLE);
          out.println();
          out.println("{");
          out.println
            ("  throw new java::lang::UnsupportedOperationException;");
          out.println("}");          
          continue;
        }

        out.print("#ifndef NO_");
        out.println(JNIGenerator.getFunctionName(methods[i]));

        if (style == PROXY_STYLE) {
          generateMethod(out, methods[i], style, metaData);
        } else if (style == PROXY_CALL_STYLE) {
          generateMethodDeclaration(out, methods[i], PROXY_STYLE);
          out.println();

          generateMethod(out, methods[i], PROXY_CALL_STYLE, metaData);
        } else {
          generateMethod(out, methods[i], INLINE_STYLE, metaData);
          out.println();

          generateMethod(out, methods[i], NORMAL_STYLE, metaData);
        }

        if (style != PROXY_STYLE) {
          out.println("#else");
          
          generateMethodPrototype(out, methods[i], NORMAL_STYLE);
          out.println("{");
          out.println
            ("  throw new java::lang::UnsupportedOperationException;");
          out.println("}");
        }

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
  
  private static void generateType3(PrintStream out, Class c, boolean struct,
                                    boolean proxy)
  {
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
      if (proxy) out.print("CNIProxy_");
      out.print(name(c));
      if (! struct) out.print("*");
    }
  }

  private static void generateType3(PrintStream out, Class c, boolean struct) {
    generateType3(out, c, struct, false);
  }

  private static void generateType3(PrintStream out, Class c) {
    generateType3(out, c, false);
  }

  private static void generateType(PrintStream out, Class c, int style) {
    if (style == PROXY_CALL_STYLE || style == PROXY_STYLE) {
      generateType3(out, c);
    } else {
      generateType(out, c);
    }
  }

  private static void generateType4(PrintStream out, Class c) {
    if (c == Void.TYPE) out.print("v");
    else if (c == Integer.TYPE) out.print("i");
    else if (c == Boolean.TYPE) out.print("z");
    else if (c == Long.TYPE) out.print("l");
    else if (c == Short.TYPE) out.print("s");
    else if (c == Character.TYPE) out.print("c");
    else if (c == Byte.TYPE) out.print("b");
    else if (c == Float.TYPE) out.print("f");
    else if (c == Double.TYPE) out.print("d");
    else if (c.isArray()) {
      generateType4(out, c.getComponentType());
      out.print("a");
    } else {
      out.print(name(c));
    }
  }

  private static boolean isSystemClass(Class c) {
    return c == Object.class || c == Class.class;
  }

  private static boolean generateLocals(PrintStream out, Method m, int style,
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
        if (style == PROXY_CALL_STYLE) out.print("CNIProxy_");
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
      generateType(out, m.getReturnType(), style);
      out.println(" rc = 0;");
    }

    return needsReturn;
  }

  private static void generateRead(PrintStream out, Method m, Class type,
                                   ParameterData data, int i, int style)
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
      out.print(") ");
      if (style == PROXY_STYLE) out.print("CNIProxy_");
      out.print("get");
      out.print(name(type));
      out.print("Fields(p");
      out.print(i);
      out.print(", &ps");
      out.print(i);
      out.println(");");
    }
  }

  private static void generateReads(PrintStream out, Method m, int style,
                                    MetaData metaData)
  {
    Class[] types = m.getParameterTypes();
    for (int i = 0; i < types.length; ++i) {
      generateRead(out, m, types[i], metaData.getMetaData(m, i), i, style);
    }
  }

  private static void generateWrite(PrintStream out, Method m, Class type,
                                    ParameterData data, int i, int style)
  {
    if (type.isPrimitive() || isSystemClass(type) || type == String.class
        || type.isArray() || data.getFlag("no_out")) return;

    out.print("  if (p");
    out.print(i);
    out.print(") ");
    if (style == PROXY_STYLE) out.print("CNIProxy_");
    out.print("set");
    out.print(name(type));
    out.print("Fields(p");
    out.print(i);
    out.print(", &ps");
    out.print(i);
    out.println(");");
  }

  private static void generateWrites(PrintStream out, Method m, int style,
                                     MetaData metaData)
  {
    Class[] types = m.getParameterTypes();
    for (int i = types.length - 1; i >= 0; --i) {
      generateWrite(out, m, types[i], metaData.getMetaData(m, i), i, style);
    }
  }

  private static void generateCallLeftSide(PrintStream out, Method m,
                                           MethodData data,
                                           boolean needsReturn, int style)
  {
    out.print("  ");
    if (m.getReturnType() != Void.TYPE) {
      if (needsReturn) {
        out.print("rc = (");
      } else {
        out.print("return (");
      }
      if (style == PROXY_STYLE) {
        generateType3(out, m.getReturnType());
      } else {
        generateType(out, m.getReturnType());
      }
      out.print(") ");

      if (m.getName().equals("CharLowerA") ||
          m.getName().equals("CharLowerW") ||
          m.getName().equals("CharUpperA") ||
          m.getName().equals("CharUpperW"))
      {
        out.print("(uintptr_t) ");
      }
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
                                            int style, MetaData metaData)
  {
    if (! data.getFlag("const")) {
      out.print("(");
      Class[] types = m.getParameterTypes();
      for (int i = paramStart; i < types.length; ++i) {
        ParameterData pdata = metaData.getMetaData(m, i);
        if (style != PROXY_CALL_STYLE && pdata.getFlag("struct")) {
          out.print("*");
        }
        String cast = cast(m, i, pdata);
        if (style != PROXY_CALL_STYLE && cast.length() > 2) {
          out.print(cast);
          out.print(" ");
        }
        if (types[i].isPrimitive() || isSystemClass(types[i])) {
          if (style == PROXY_CALL_STYLE || cast.length() <= 2) {
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
          if (style == PROXY_CALL_STYLE || cast.length() <= 2) {
            out.print("(");
            generateType3(out, types[i], false);
            out.print(") ");
          }
          if (style == PROXY_STYLE) {
            out.print("p");
            out.print(i);
          } else {
            out.print("(p");
            out.print(i);
            out.print(" ? elements(p");
            out.print(i);
            out.print(") : 0)");
          }
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
                                          int style, MetaData metaData)
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
      generateCallLeftSide(out, m, data, needsReturn, style);
      out.print("procedure");
      generateCallRightSide(out, m, data, 0, style, metaData);
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
      generateCallLeftSide(out, m, data, needsReturn, style);
      out.print("(*procedure)");
      generateCallRightSide(out, m, data, 0, style, metaData);
      out.println();
      out.println("    }");
    } else {
      out.println("    static bool initialized = false;");
      out.println("    static void* handle = 0;");
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
      generateCallLeftSide(out, m, data, needsReturn, style);
      out.print("(*procedure)");
      generateCallRightSide(out, m, data, 0, style, metaData);
      out.println();
      out.println("    }");
    }

    out.println("  }");
  }

  private static boolean mightBeMacro(Method m, MethodData data) {
    String name = name(m);
    return name.endsWith("_sizeof");
  }

  private static void generateNormalCall(PrintStream out, Method m,
                                         MethodData data, int style)
  {
    if (style == PROXY_CALL_STYLE) out.print("CNIProxy_");

    String accessor = data.getAccessor();
    if (style != PROXY_CALL_STYLE && accessor.length() != 0) {
      out.print(accessor);
    } else {
      out.print(name(m));
    }
    
    if (style == PROXY_CALL_STYLE) generateDecoration(out, m);
  }

  private static void generateCall(PrintStream out, Method m, MethodData data,
                                   boolean needsReturn, boolean asMacro,
                                   int style, MetaData metaData)
  {
    String copy = (String) data.getParam("copy");
    boolean makeCopy = copy.length() != 0 && m.getReturnType() != Void.TYPE;
    if (makeCopy) {
      out.print("  ");
      out.print(copy);
      out.print(" temp = ");
    } else {
      generateCallLeftSide(out, m, data, needsReturn, style);
    }

    int paramStart = 0;
    String name = name(m);

    if (style == PROXY_CALL_STYLE) {
      generateNormalCall(out, m, data, style);
    } else {
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
            out.print(name.substring(0, index));
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
        generateNormalCall(out, m, data, style);
      }
    }
    
    generateCallRightSide(out, m, data, paramStart, style, metaData);
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
      generateType(out, m.getReturnType(), style);
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

  private static void generateDecoration(PrintStream out, Method m) {
    Class[] parameters = m.getParameterTypes();
    for (int i = 0; i < parameters.length; ++i) {
      out.print("_");
      generateType4(out, parameters[i]);
    }
  }

  private static void generateMethodPrototype(PrintStream out, Method m,
                                              int style)
  {
    if (style == INLINE_STYLE) {
      out.print("inline ");
    } else if (style == PROXY_STYLE) {
      out.print("extern \"C\" ");
    }

    if (style == PROXY_STYLE) {
      generateType3(out, m.getReturnType());
    } else {
      generateType(out, m.getReturnType());
    }
    out.println();

    if (style == INLINE_STYLE) {
      out.print("inline_");
    } else if (style == PROXY_STYLE) {
      out.print("CNIProxy_");
    } else {
      generateTypeName(out, m.getDeclaringClass());
      out.println("::");
      out.print("MacroProtect_");
    }

    out.print(m.getName());
    if (style == PROXY_STYLE) {
      generateDecoration(out, m);
    }
    out.println();

    out.print("  (");
    Class[] parameters = m.getParameterTypes();
    for (int i = 0; i < parameters.length; ++i) {
      if (style == PROXY_STYLE) {
        generateType3(out, parameters[i], false, true);
      } else {
        generateType(out, parameters[i]);
      }
      out.print(" p");
      out.print(i);
      if (i < parameters.length - 1) out.print(", ");
    }
    out.print(")");
  }

  private static void generateMethodDeclaration(PrintStream out, Method m,
                                                int style)
  {
    generateMethodPrototype(out, m, style);
    out.println(";");
  }

  private static void generateMethod(PrintStream out, Method m,
                                     int style, MetaData metaData)
  {
    generateMethodPrototype(out, m, style);    
    out.println();

    out.println("{");

    MethodData data = metaData.getMetaData(m);

    String name = name(m);
    Class[] types = m.getParameterTypes();

    if (style == INLINE_STYLE || style == PROXY_STYLE
        || style == PROXY_CALL_STYLE)
    {
      boolean isGTKmemove = name.equals("memmove") && types.length == 2 &&
        m.getReturnType() == Void.TYPE;
      if (isGTKmemove) {
        generateGTKmemmove(out, m);
      } else {
        boolean needsReturn = generateLocals(out, m, style, metaData);
        generateReads(out, m, style, metaData);
        if (data.getFlag("dynamic")) {
          generateDynamicCall(out, m, data, needsReturn, style, metaData);
        } else if (mightBeMacro(m, data)) {
          out.print("#ifdef ");
          out.println(name(m));
          generateCall(out, m, data, needsReturn, true, style, metaData);
          out.println("#else");
          generateCall(out, m, data, needsReturn, false, style, metaData);
          out.println("#endif");
        } else {
          generateCall(out, m, data, needsReturn, false, style, metaData);
        }
        generateWrites(out, m, style, metaData);
        generateReturn(out, m, needsReturn);
      }
    } else {
      if (TRACE_FUNCTIONS) {
	out.print(" printf(\"Called %s\\n\", \"" + name + "\");");
      }
      out.print("  return inline_");
      out.print(m.getName());
      out.print("(");  
      Class[] parameters = m.getParameterTypes();
      for (int i = 0; i < parameters.length; ++i) {
        out.print("p");
        out.print(i);
        if (i < parameters.length - 1) out.print(", ");
      }
      out.println(");");
    }

    out.println("}");
  }

  private static void generateSystemIncludes(PrintStream out) {
    out.println("#include \"java/lang/UnsupportedOperationException.h\"");

    if (SWT.getPlatform().equals("gtk")) {
      out.println("#include \"cairo_custom.h\"");
      out.println("#include \"cairo.h\"");
      out.println("#include \"cairo-xlib.h\"");
      out.println("#include \"glx.h\"");
    } else if (SWT.getPlatform().equals("carbon")) {
      out.println("#include \"NSGeometry.h\"");
      out.println("#include \"AGL/agl.h\"");
      out.println("#include \"objc/objc-runtime.h\"");
      out.println("#include \"Carbon/Carbon.h\"");
      out.println("#include \"HIWebView.h\"");
      out.println("#include \"CarbonUtils.h\"");
    } else if (SWT.getPlatform().equals("win32")) {
      out.println("#include \"windows.h\"");
      out.println("#include \"docobj.h\"");
      out.println("#include \"commctrl.h\"");
      out.println("#include \"stdint.h\"");
      out.println("#include \"com_custom.h\"");
    }
    out.println("#include \"os.h\"");
    out.println();

    out.println("#undef TRUE");
    out.println("#define TRUE 1");
    out.println();
  }

  private static void generateForeignSystemIncludes(PrintStream out) {
    if (SWT.getPlatform().equals("win32")) {
      out.println("#include \"windows.h\"");
      out.println("#include \"docobj.h\"");
      out.println("#include \"commctrl.h\"");
      out.println("#include \"gdiplus.h\"");
      out.println("#include \"stdint_compat.h\"");
      out.println("using namespace Gdiplus;");
      out.println();
    }
  }

  private static void generateAll(String prefix, boolean aggregate) 
    throws Exception
  {
    PrintStream nativeOut;
    PrintStream foreignOut;
    PrintStream foreignDefOut = null;

    if (aggregate) {
      nativeOut = nativeOut(prefix);
      foreignOut = foreignOut(prefix);
      foreignDefOut = foreignDefOut(prefix);
    } else {
      nativeOut = headerOut(prefix, NORMAL_STYLE);
      foreignOut = headerOut(prefix, PROXY_STYLE);
    }

    MyGeneratorApp app = new MyGeneratorApp
      (nativeOut, foreignOut, foreignDefOut, prefix, aggregate);
    try {
      System.out.println("first stage:");

      generateForeignSystemIncludes(foreignOut);

      app.stage = 1;
      app.generateAll();

      generateSystemIncludes(nativeOut);
      
      System.out.println("\nsecond stage:");
      app.stage = 2;
      app.generateAll();

      if (! aggregate) {
        nativeOut.println("#endif");
        foreignOut.println("#endif");
      }

      System.out.println("\nthird stage:");
      app.stage = 3;
      app.generateAll();
    } finally {
      nativeOut.close();
      foreignOut.close();
      if (foreignDefOut != null) foreignDefOut.close();
    }
  }

  private static void usageAndExit() {
    System.err.println
      ("usage: java " + CNIGenerator.class.getName() +
       " [-aggregate] <output_prefix>");
    System.exit(-1);
  }

  public static void main(String[] args) throws Exception {
    boolean aggregate = false;
    String prefix = null;

    for (int i = 0; i < args.length; ++i) {
      String s = args[i];
      if (s.equals("-aggregate")) {
        aggregate = true;
      } else {
        if (prefix != null) usageAndExit();
        prefix = s;
      }
    }

    if (prefix == null) usageAndExit();

    generateAll(prefix, aggregate);
  }

  private static class MyGeneratorApp extends JNIGeneratorApp {
    public final PrintStream nativeOut;
    public final PrintStream foreignOut;
    public final PrintStream foreignDefOut;
    public final String prefix;
    public final boolean aggregate;
    public int stage = 1;
    
    private MyGeneratorApp(PrintStream nativeOut, PrintStream foreignOut,
                           PrintStream foreignDefOut, String prefix,
                           boolean aggregate)
    {
      this.nativeOut = nativeOut;
      this.foreignOut = foreignOut;
      this.foreignDefOut = foreignDefOut;
      this.prefix = prefix;
      this.aggregate = aggregate;
    }

    public void generate() {
      if (getMainClassName().contains(".cde.") ||
          getMainClassName().contains(".gnome.") ||
          getMainClassName().contains(".mozilla."))
      {
        return;
      }
      
      try {
        switch (stage) {
        case 1:
          generateIncludes(nativeOut, getStructureClasses());
          generateIncludes(nativeOut, getNativesClasses());
          if (! aggregate) {
            generateProxyIncludes(foreignOut, getStructureClasses());
          }
          break;

        case 2:
          if (! aggregate) {
            generateStructureFunctionIncludes(nativeOut, getStructureClasses(),
                                              getMetaData());
          }
          generateStructureHeaders(nativeOut, foreignOut, prefix,
                                   getStructureClasses(), getMetaData());
          break;

        case 3:
          generateStructureFunctions(nativeOut, foreignOut, prefix,
                                     getStructureClasses(), getMetaData());
          generateNatives(nativeOut, foreignOut, prefix, getNativesClasses(),
                          getMetaData());
          if (foreignDefOut != null) {
            generateNativeDefs(foreignDefOut, getNativesClasses(),
                               getMetaData());
          }
          break;

        default: throw new RuntimeException("unexpected stage: " + stage);
        }
      } catch (RuntimeException e) {
        throw e;
      } catch (Exception e) {
        throw new RuntimeException(e);
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
