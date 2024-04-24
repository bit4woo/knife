package runcmd;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;


public class ClassUtils {

	public ClassUtils() {
		// TODO Auto-generated constructor stub
	}
	
	public static List<String> getPublicStaticFinalStringFields(Class classObj) {
		List<String> result = new ArrayList<>();
		Field[] fields = classObj.getDeclaredFields();
		for (Field field : fields) {
			if (isPublicStaticFinalString(field)) {
				try {
					result.add((String) field.get(null));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	public static List<String> getStringFields(Class classObj) {
		List<String> result = new ArrayList<>();
		Field[] fields = classObj.getDeclaredFields();
		for (Field field : fields) {
			if (field.getType() == String.class) {
				try {
					result.add((String) field.get(null));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}
	
	private static boolean isPublicStaticFinalString(Field field) {
		int modifiers = field.getModifiers();
		return java.lang.reflect.Modifier.isPublic(modifiers)
				&& java.lang.reflect.Modifier.isStatic(modifiers)
				&& java.lang.reflect.Modifier.isFinal(modifiers)
				&& field.getType() == String.class;
	}
}
