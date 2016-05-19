package org.witchcraft.generatorutil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import org.eclipse.uml2.uml.Property;

public class RandomValueGenerator {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy.MM.dd HH:mm:ss z");

	static String randStrings[] = { "alpha", "beta", "gamma", "delta", "theta",
			"zeta", "epsilon", "pi", "John", "Wilson", "Mark", "Eric",
			"Malissa", "Lavendar" };
	
	static String[] stringTypes = {"String","nameType", "uniqueNameType", "largeText"};

	public static Object getRandomValue(Property attribute, String counter) {

		String typeName = attribute.getType().getName();
		Random generator = new Random(19580427);
		

		if (Arrays.asList(stringTypes).contains(typeName)) {

			String uniqueNum = "";
			/*
			 * if(attribute instanceof Column){ if(
			 * ((Column)attribute).isUnique() ) uniqueNum = new Long(new
			 * Random().nextInt(100000)).toString(); }
			 */
			//return "\"" + randStrings[new Random().nextInt(randStrings.length)]
			//		+ uniqueNum + "\"";
			if(counter.equals(""))
				counter = new Long(new Random().nextInt(100000)).toString(); 
			
			return "\""  + attribute.getName() + "-" + System.currentTimeMillis() + "-" +  counter + "\"";
			
			
		} else if (typeName.contains("Date")) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTimeInMillis(new Date().getTime()
					- new Random().nextInt(100000) * 200000);

			return "dateFormat.parse(\"" + dateFormat.format(cal.getTime())
					+ "\")";
		} else if (typeName.equalsIgnoreCase("int")
				|| typeName.equalsIgnoreCase("Integer")) {
			return (attribute.getDefault() == null) ? new Random()
					.nextInt(100000) : attribute.getDefault();
		} else if (typeName.equalsIgnoreCase("double")
				|| typeName.equalsIgnoreCase("BigDecimal")) {
			DecimalFormat decimalFormat = new DecimalFormat();

			return (attribute.getDefault() == null) ? Math
					.round(100 * 100 * new Random().nextDouble()) / 100.00
					: attribute.getDefault();
		} else if (typeName.equalsIgnoreCase("boolean")) {
			return (attribute.getDefault() == null) ? new Random()
					.nextBoolean() : attribute.getDefault();
					
		} /*else if (attribute.getType()..getSimpleName()
				.equalsIgnoreCase("Enumeration")) {
			
			ElementSet literals = ((Enumeration) attribute.Type()).Literal();
			Element element = (Element) literals.get(new Random()
					.nextInt(literals.size()));
			return ClassUtil.getPackageName((Enumeration) attribute.Type())
					+ "." + attribute.Type().Name() + "." + element.getName();
		}*/

		return "null" + "/*unknown attrib type:" + attribute.getType().getName() +
	   "*/";
	}

}
