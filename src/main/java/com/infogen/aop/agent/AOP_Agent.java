/**
 * 
 */
package com.infogen.aop.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.Map;

import com.infogen.aop.tools.Tool_Jackson;

/**
 * @author larry/larrylv@outlook.com/创建时间 2015年2月27日 上午11:47:39
 * @since 1.0
 * @version 1.0
 */
public class AOP_Agent {
	private transient static String add_transformer_lock = "";

	public static void agentmain(String args, Instrumentation inst) {
		// Caused by: java.lang.ClassCastException: com.infogen.aop.agent.InfoGen_Agent_Advice_Class cannot be cast to com.infogen.aop.agent.InfoGen_Agent_Advice_Class
		Agent_Advice_Class infogen_agent_advice_class = null;
		synchronized (add_transformer_lock) {
			Class<?>[] allLoadedClasses = inst.getAllLoadedClasses();
			for (Class<?> loadedClasse : allLoadedClasses) {
				if (loadedClasse.getName().equals("com.infogen.aop.agent.InfoGen_Agent_Cache")) {
					try {
						Field field = loadedClasse.getField("class_advice_map");
						@SuppressWarnings("unchecked")
						Map<String, String> class_advice_map = (Map<String, String>) field.get(loadedClasse);
						for (String infogen_advice : class_advice_map.values()) {
							infogen_agent_advice_class = Tool_Jackson.toObject(infogen_advice, Agent_Advice_Class.class);
							for (Class<?> clazz : allLoadedClasses) {
								String class_name = infogen_agent_advice_class.getClass_name();
								if (clazz.getName().equals(class_name)) {
									try {
										AOP_Transformer transformer = new AOP_Transformer(infogen_agent_advice_class, clazz);
										inst.addTransformer(transformer, true);
										System.out.println("重新加载class文件 -> " + class_name);
										inst.retransformClasses(clazz);
										inst.removeTransformer(transformer);
									} catch (Exception e) {
										System.out.println("重新加载class文件失败 :");
										e.printStackTrace();
									}
								}
							}
						}
					} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException | IOException e) {
						System.out.println("重新加载class文件失败 :");
						e.printStackTrace();
					}
				}
			}
		}

	}

	public static void premain(String agentArgs, Instrumentation inst) {
	}

	public static void help() {
		System.out.println("eg -> ");
		System.out.println("Infogen_Agent_Advice infogen_advice = new Infogen_Agent_Advice();");
		System.out.println("infogen_advice.setClass_name(class_name);");
		System.out.println("infogen_advice.setMethod_name(method_name);");
		System.out.println("infogen_advice.setInsert_before(\"Integer i = ($w)6;System.out.println($1+i);\");");
		System.out.println("infogen_advice.setInsert_after(\"System.out.println(\"after\");\");");
		System.out.println("infogen_advice.setAdd_catch(\"System.out.println(\"error\");throw $e;\");");
		System.out.println("InfoGen_Agent.class_advice_map.put(class_name, infogen_advice);");
		System.out.println("vm.loadAgent(Infogen_Agent_Path.path(),class_name);");
	}
}