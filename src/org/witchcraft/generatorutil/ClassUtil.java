package org.witchcraft.generatorutil;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityFinalNode;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.ControlFlow;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.eclipse.xtend.XtendFacade;
import org.eclipse.xtend.typesystem.uml2.UML2MetaModel;
import org.eclipse.xtend.typesystem.uml2.profile.ProfileMetaModel;

public class ClassUtil {

	private static final String WORKFLOW_PROPERTIES = "workflow.properties";
	public static final Logger log = Logger.getLogger(ClassUtil.class);
	Operation operation;

	static XtendFacade xtendFacade;
	static Properties properties = new Properties();

	static int count = 0;  

	private static final Logger logger = Logger.getLogger(ClassUtil.class);

	private static final String DOT_NET = "DOT_NET";

	static Map<String, String[]> mapTypes = new HashMap<String, String[]>();

	// Logger logger = Logger.getLogger(ClassUtil.class);

	private static Model model;

	static {

		xtendFacade = XtendFacade.create("template::GeneratorExtensions");
		UML2MetaModel mm = new UML2MetaModel();
		ProfileMetaModel pmm = new ProfileMetaModel();
		loadProperties();

		// pmm.setProfile(properties.getProperty("model.dir")
		// + "/wcprofile.profile.uml");
		xtendFacade.registerMetaModel(mm);
		xtendFacade.registerMetaModel(pmm);
	}

	// state variables
	private static Class currentEntity;

	private static Property currentEmbeddable;

	private static String currentEmbeddableName;

	private static String currentCartridge = null;

	private static Boolean currentMultiMode = false;

	private static Boolean currentTemplateMode = false;

	private static Boolean currentContainer = false;

	public static String getCurrentCartridge() {
		if (currentCartridge == null) {
			currentCartridge = readProperty("cartridge");
		}
		return currentCartridge;
	}

	public static void setCurrentCartridge(String currentCartridge) {

		ClassUtil.currentCartridge = currentCartridge;
	}

	static {

		// loadProperties();
		getCurrentCartridge();

		if (DOT_NET.equals(currentCartridge)) {
			// system.out.println("Setting Current Cartridge to " +
			// currentCartridge);
			mapTypes.put("Integer", new String[] { "int", "" });
			mapTypes.put("Date", new String[] { "DateTime", "" });
			mapTypes.put("datatypes.Integer", new String[] { "int", "" });
			mapTypes.put("imageFile", new String[] { "FileStream", "" });
			mapTypes.put("Long", new String[] { "long", "" });
		} else {
			mapTypes.put("datatypes.Integer", new String[] { "Integer", "" });
			mapTypes.put("imageFile", new String[] { "FileAttachment", "" });
			mapTypes.put("Currency", new String[] { "BigDecimal", "" });
			mapTypes.put("DateTime", new String[] { "Date", "" });
			mapTypes.put("long", new String[] { "Long", "" });
			
		}

		// TODO maxsize should be configurable
		mapTypes.put("largeText", new String[] { "String", "@Lob" });
		mapTypes.put("nameType", new String[] { "String",
				"@NotNull @Size(min=1, max=50)" });
		mapTypes.put("uniqueNameType", new String[] { "String",
				"@NotNull @Size(min=1, max=50)  " });

	}

	private static Boolean currentEditMode = false;

	public static Boolean isCurrentMultiMode() {
		return currentMultiMode;
	}

	public static void setCurrentMultiMode(Boolean currentMultiMode) {
		ClassUtil.currentMultiMode = currentMultiMode;
	}

	public static Boolean isCurrentEditMode() {
		return currentEditMode;
	}

	public static void setCurrentEditMode(Boolean currentEditMode) {
		ClassUtil.currentEditMode = currentEditMode;
	}

	public static Property getCurrentEmbeddable() {
		// if(currentEmbeddable != null)
		// //system.out.println("getting embeddable -> " +
		// currentEmbeddable.getName());
		return currentEmbeddable;
	}

	public static String getCurrentEmbeddableName() {
		return currentEmbeddableName;
	}

	public static void setCurrentEmbeddableName(String ce) {
		currentEmbeddableName = ce;
	}

	public static void setCurrentEmbeddable(Property currentEmbeddable) {
		// //system.out.println("setting embeddable to " +
		// currentEmbeddable.getName());
		ClassUtil.currentEmbeddable = currentEmbeddable;
		setCurrentEmbeddableName(currentEmbeddable.getName());

	}

	public static void clearCurrentEmbeddable() {
		ClassUtil.currentEmbeddable = null;
	}

	public static Class getCurrentEntity() {
		return currentEntity;
	}

	public static void setCurrentEntity(Class currentEntity) {
		ClassUtil.currentEntity = currentEntity;
	}

	public static void main(String[] args) {
		String ret = elToJava("address.phone");

		BasicEList list = new BasicEList();
		System.out.println("sss");
		list.add("ji");

		for (Object object : list) {
			System.out.println(object);
		}
		// system.out.println(ret);
	}

	public static int getRandomNumber() {
		return new Random().nextInt(15000);

	}

	private static final int VERSION = 1;

	public static String serialver(Class cls) {
		// cls.
		return new Integer(
				(cls.getName() + "-" + cls.getPackage().getName()).hashCode()
						^ VERSION).toString()
				+ "L";
	}

	/*
	 * public static String getParentDisplayNameRecursive(Class cls ){
	 * 
	 * if(cls.parents().isEmpty()) return ""; StereotypeType st =
	 * cls.getAppliedStereotype("AbstractEntity");
	 * 
	 * cls.allParents(); }
	 */

	public static List<Property> getAllAttribs(Class cls) {
		List<Property> lstAttributes = new ArrayList<Property>();

		EList<Class> classes = cls.getSuperClasses();
		try {
			for (Class classifier : classes) {
				// Constraint cs;
				// cs.
				// //system.out.println("found " +
				// classifier.getAllAttributes().size());
				lstAttributes.addAll(classifier.getAllAttributes());

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		lstAttributes.addAll(cls.getAllAttributes());
		return lstAttributes;
	}

	/**
	 * Returns a comma delimited string with , after every list member except
	 * for the last
	 * 
	 * @return
	 */
	public static String getCommaDelimitedString(List<String> listStrings,
			int offset) {
		StringBuffer buffer = new StringBuffer();

		for (int i = 0; i < listStrings.size(); i++) {
			String param = listStrings.get(i);

			if (StringUtils.isEmpty(param))
				continue;

			buffer.append(param);

			if (i < (listStrings.size() - offset)) // add comma to all but last
				buffer.append(", ");
		}
		return buffer.toString();
	}

	public static List<String> getListFromCommaDeleimtedString(String arg) {
		String[] arr = arg.split(",");
		int i = 0;
		for (String string : arr) {
			string = string.trim();
			arr[i++] = string;
		}
		return Arrays.asList(arr);
	}

	/**
	 * Returns a list containing lists of string that are in the format {a,
	 * b,c}{d, e, f} - the sublists are comma delimited while the top level is
	 * {} delimited
	 * 
	 * @param arg
	 * @return
	 */
	public static List<List<String>> getListOfLists(String arg) {
		List<List<String>> list = new ArrayList<List<String>>();
		String[] arr = arg.split("\\{|\\}| }");

		for (String string : arr) {
			if (!StringUtils.isEmpty(string))
				list.add(getListFromCommaDeleimtedString(string));
		}
		return list;
	}

	public static String getSubString(String arg, String indexStr) {
		return arg.substring(arg.indexOf("_") + 1);
	}

	public static String getParametersSignatureRest(Operation op, String type) {

		List<Parameter> params = op.getOwnedParameters();

		List<String> lstStrings = new ArrayList<String>();

		for (int i = 0; i < params.size(); i++) {
			Parameter param = params.get(i);
			String result = (String) xtendFacade.call("fqn",
					new Object[] { param.getType() });
			if (!StringUtils.isEmpty(param.getName()))
				lstStrings.add("@" + type + "(" + "\"" + param.getName()
						+ "\")" + " " + result + " " + param.getName());
		}

		return getCommaDelimitedString(lstStrings, 1);
	}

	public static String getParametersSignature(Operation op) {
		return getParametersSignature(op, "");
	}

	/**
	 * For an operation will return comma delimited parameters with their type
	 * names - e.g. String firstName, String lastname
	 * 
	 * @param op
	 * @return
	 */
	public static String getParametersSignature(Operation op, String ext) {
		// System.out.println("PARAM is null");
		op.getOwnedParameters();

		List<Parameter> params = op.getOwnedParameters();
		StringBuffer buffer = new StringBuffer();
		List<String> lstStrings = new ArrayList<String>();

		for (int i = 0; i < params.size(); i++) {
			Parameter param = params.get(i);
			String result = (String) xtendFacade.call("fqn",
					new Object[] { param.getType() });
			if (!StringUtils.isEmpty(param.getName()))
				lstStrings.add(ext + " " + result + " " + param.getName());
		}

		return getCommaDelimitedString(lstStrings, 1);
	}

	public static String getParameters(Operation op) {
		List<Parameter> params = op.getOwnedParameters();
		StringBuffer buffer = new StringBuffer();
		List<String> lstStrings = new ArrayList<String>();

		for (int i = 0; i < params.size(); i++) {
			Parameter param = params.get(i);
			if (!StringUtils.isEmpty(param.getName()))
				lstStrings.add(param.getName());
		}

		return getCommaDelimitedString(lstStrings, 1);
	}

	public static String getOpReturnType(Operation op) {

		if (op.getType() == null)
			return "void";

		// system.out.println("opret" + op.getType().getName());
		// system.out.println(op.getType().getPackage().getName());

		if (op.getType().getName() == null)
			return "void";

		String name = op.getType().getName();
		if (op.getType().getPackage().getName().startsWith("collections")) {
			if (op.getTemplateParameter() != null)
				return name + "<" + op.getTemplateParameter().getSignature()
						+ ">";
		}
		return "name";
	}

	public static String getInterfaces(Class clazz) {
		/*
		 * StringBuilder buffer = new StringBuilder(); List<Interface>
		 * interfaces = clazz.getImplementedInterfaces();
		 * 
		 * if (!interfaces.isEmpty()) buffer.append(" implements ");
		 * 
		 * List<String> lstStrings = new ArrayList<String>();
		 * 
		 * for (int i = 0; i < interfaces.size(); i++) { Interface param =
		 * interfaces.get(i); String result = (String) xtendFacade.call("fqn",
		 * new Object[] { param }); lstStrings.add(result); }
		 * 
		 * return buffer.append(getCommaDelimitedString(lstStrings,
		 * 2)).toString();
		 */

		return ""; // TODO
	}

	public static String getValidatorAnnotations(Property prop) {
		return StringUtils.EMPTY;
	}

	public static String getTypeName(String typeName) {
		if (mapTypes.containsKey(typeName)) {
			return mapTypes.get(typeName)[0];
		}
		return typeName;
	}

	/**
	 * Annotation for the mapped type e.g for largetText we need to return
	 * "@Lob"
	 * 
	 * @param typeName
	 * @return
	 */
	public static String getTypeAnnotation(String typeName) {
		if (mapTypes.containsKey(typeName)) {
			//log.info(typeName + " mapped to annotation "+ mapTypes.get(typeName)[1]);
			return mapTypes.get(typeName)[1];
		}
		return "";
	}

	public static String getPackageName(Class cls) {
		String result = "";
		for (Package pck = cls.getPackage(); pck != null; pck = pck
				.getNestingPackage()) {
			result = pck.getName() + (result.length() > 0 ? "." + result : "");
		}
		return result;
	}

	public static String removeSpaces(String target) {
		target = target.replace(" ", "");
		return target;
	}

	/**
	 * This function tries to split a camel case variable name into space
	 * delimited user displayable string e.g.
	 * 
	 * @return input firstName - output First Name
	 */
	public static String getViewLabelFromVariable(String varName) {
		return getSplitName(varName, " ");
	}

	/**
	 * This function tries to split a camel case variable name into space
	 * delimited user displayable string e.g.
	 * 
	 * @return input firstName - output First Name
	 */
	public static String getSplitName(String varName,
			String concatChar) {

		if (varName == null) {
			// system.out.println("Warn: null variable in getViewLabel ");
			return "";
		}else 
			varName = varName.trim();
		
		String result =  varName.replaceAll(String.format("%s|%s|%s",
				"(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
				"(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
		result = result.replaceAll(" ", concatChar);
		
		return WordUtils.capitalizeFully(result);
	}

	public static String getColumnNameUC(String varName) {
		return getSplitName(varName, "_").toUpperCase();
	}

	public static String readProperty(String key) {
		if (properties == null) {
			if (!loadProperties())
				return "COULDNT_READ_FILE";
		}

		String value = properties.getProperty(key);
		// logger.info("Returning value " + value + " for key " + key);
		return value;
	}

	public static String readProperty(String key, String def) {
		String ret = readProperty(key);
		if (ret == null || ret.equals(""))
			ret = def;
		return ret;

	}

	public static String getDisplayNameFromAttribs(Class cls) {
		EList<Property> attribs = getMineAndParentsAttributes(cls);

		// String firstProp = if( attribs.get(0).getName();

		for (Property property : attribs) {
			if (property.getName().contains("name")
					&& property.getAssociation() == null
					&& isStringType(property.getType().getName()))
				return property.getName();
		}

		// //system.out.println("after first loop");
		for (Property property : attribs) {
			if (property.getType() != null) {

				if (property.getAssociation() == null
						&& isStringType(property.getType().getName()))
					return property.getName();
			}
		}

		// couldnt find any suitable display name
		return attribs.get(0).getName() + "+ \"\"";
		
	}

	public static List<Property> attribsOfThisClass(Class cls) {
		List<Property> target = new ArrayList<Property>();
		return getAttribs(cls, target, null);
		// return target;
	}

	public static List<Property> getAttribs(Class cls, List<Property> props,
			String name) {
		List<Property> properties = new ArrayList();
		properties.addAll(cls.getAllAttributes());

		// cls.getat

		EList<Class> classes = cls.getSuperClasses();
		if (classes != null) {
			for (Class class1 : classes) {
				try {
					properties.addAll(class1.getAllAttributes());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		for (Property property : properties) {
			if (property.getAssociation() != null
					&& (property.getType().getAppliedStereotype(
							"wcprofile::Embeddable") != null || property
							.getAssociation().getAppliedStereotype(
									"wcprofile::ContainedAssociation") != null)) {

				getAttribs((Class) property.getType(), props,
						property.getName());
			} else {
				// //system.out.println("deployment is " + name);
				property.createDeployment(name);
				props.add(property);
			}
		}

		return props;
	}

	public static String getDeployName(Property prop) {
		// //system.out.println( prop.getClass_().getName() + " " +

		if (prop.getDeployments().size() > 0
				&& !prop.getClass_().getName()
						.equals(getCurrentEntity().getName())) {
			return prop.getDeployments().get(0).getName();
		}
		return "";
	}

	private static boolean loadProperties() {
		properties = new Properties();
		try {
			// InputStream stream = ClassUtil.class
			// .getResourceAsStream("workflow.properties");
			properties.load(new FileInputStream(WORKFLOW_PROPERTIES));
			if (properties == null) {
				logger.error("workflow properties file is not in the classpath");
				return false;
			}
			// properties.load(stream);
			return true;
		} catch (Exception e) {
			logger.error("error loading properties", e);
			e.printStackTrace();
			return false;
		}
	}

	public static EList<ActivityEdge> outgoing(ActivityNode act) {
		return act.getOutgoings();
	}

	public static String getSwimlane(ActivityNode act) {
		if (act.getInPartitions().size() > 0)
			return act.getInPartitions().get(0).getName();
		return null;
	}

	private static int finalNodeCounter;

	public static String getTaskMassagedName(NamedElement act) {

		// Date today = new Date();

		if (act.getName() == null || act.getName() == "") {

			if (act instanceof ControlFlow) {
				ControlFlow cf = (ControlFlow) act;
				act.setName(cf.getGuard().stringValue());
			}

			if (act instanceof ActivityFinalNode) {
				ActivityFinalNode cf = (ActivityFinalNode) act;
				act.setName("endState" + ++finalNodeCounter);
			}

			if (act instanceof ForkNode) {
				ForkNode forkNode = (ForkNode) act;
				forkNode.setName("from"
						+ forkNode.getIncomings().get(0).getName() + "Fork");
			}

		}

		act.setName(massagedName(act.getName()));
		// //system.out.println(act.getName());
		return act.getName();
	}

	public static String massagedName(String orgName) {
		// String orgName = act.getName();
		// //system.out.println(act.getName());vf
		String dest = orgName.trim();

		dest = orgName.replace("/", "Or");
		dest = dest.replace("\\", "Or");
		dest = dest.replace("=", "Is");
		dest = dest.replace(" ", "");
		dest = dest.replace("+", "And");
		// act.setName(dest);

		// system.out.println(dest);
		dest = StringUtils.uncapitalize(dest);
		// system.out.println(dest);
		return dest;
	}

	public static String getSingular(String word) {
		return word.endsWith("s") ? word.substring(0, word.length() - 1) : word;
	}

	public static boolean isStringType(String name) {
		return (name.equalsIgnoreCase("String")
				|| name.equalsIgnoreCase("nameType")
				|| name.equalsIgnoreCase("uniqueNameType") || name
					.equalsIgnoreCase("largeText"));
	}

	public static boolean isAggregate(Property prop) {
		return prop.getAggregation().getValue() == AggregationKind.SHARED;
	}

	public static int getCounter() {
		return count++;
	}

	public static String resetCounter() {
		count = 0;
		return "";
	}

	public static void print(String message) {
		// system.out.println(message);
		logger.info(message);
	}

	public static String getTreeParent(String arg) {
		// system.out.println("arg is " + arg);
		String[] tokens = arg.split(",");
		if (tokens.length < 2) {
			// system.out.println("Invalid tree field in the model");
		}
		return tokens[0].trim();
	}

	public static String getTreeChildren(String arg) {
		String[] tokens = arg.split(",");
		if (tokens.length < 2) {
			// system.out.println("Invalid tree field in the model");
		}
		return tokens[1].trim();
	}

	public static String getTreeDetails(String arg) {
		String[] tokens = arg.split(",");
		if (tokens.length < 3) {
			log.warn("Invalid tree field in the model / or no details provided "
					+ arg);
		}
		return tokens[2].trim();
	}

	public static String appendBraces(String str) {
		str = str.trim();
		if (!str.startsWith("("))
			str = "(" + str;
		if (!str.endsWith(")"))
			str = str + ")";
		return str;
	}

	public static Property getAttrib(Class cls, String name) {
		return cls.getAttribute(name, null);
	}

	private static String[] arrString = { "One", "Two", "Three", "Four", "Five" };

	public static List getCounters() {
		return Arrays.asList(arrString);
	}

	// for report group generation we need to seperate calc from field
	public static String getCalc(String t) {
		return t.split(":")[0].trim();
	}

	public static String getComponentName(String arg) {
		String[] arr = arg.split("/.");
		return "";
	}

	// for report group generation we need to seperate calc from field
	public static String getField(String t) {
		String[] arr = t.split(":");
		return (arr.length > 1) ? arr[1].trim() : "";
	}

	public static Map<String, String> mapFieldsForReports = new HashMap<String, String>();

	public static String getFieldTypeForReports(String key) {
		mapFieldsForReports.put("String", "java.lang.String");
		mapFieldsForReports.put("nameType", "java.lang.String");
		mapFieldsForReports.put("uniqueNameType", "java.lang.String");
		mapFieldsForReports.put("largeText", "java.lang.String");
		mapFieldsForReports.put("long", "java.lang.Long");
		mapFieldsForReports.put("Long", "java.lang.Long");
		mapFieldsForReports.put("Double", "java.lang.Double");
		mapFieldsForReports.put("Date", "java.util.Date");
		mapFieldsForReports.put("Boolean", "java.lang.Boolean");
		mapFieldsForReports.put("Integer", "java.lang.Integer");

		if (mapFieldsForReports.containsKey(key))
			return mapFieldsForReports.get(key);

		// system.out.println("Couldnt find report type for " + key);
		return key;

		// return "java.lang.String";
	}

	static String[] primitives = { "date", "long", "integer", "boolean",
			"double", "float", "string" };

	public static boolean isPrimitive(Type type) {
		String name = type.getName().toLowerCase();
		return Arrays.asList(primitives).contains(name);
	}

	/**
	 * for an expression like customer.address.phone we need to return
	 * getCustomer().getAddress().setPhone
	 * 
	 * @param arg
	 * @return
	 */
	public static String elToJava(String arg) {
		String[] arr = arg.split("\\.");
		int i = 0;
		for (String string : arr) {
			arr[i++] = StringUtils.capitalize(string);
		}

		for (i = 0; i < arr.length - 1; i++) {
			arr[i] = "get" + arr[i] + "().";
		}
		arr[arr.length - 1] = "set" + arr[arr.length - 1];

		StringBuffer sb = new StringBuffer();
		for (String string : arr) {
			sb.append(string);
		}

		return sb.toString();
	}

	public static Class getFirstChild(Class c) {

		Model m = c.getModel();
		EList<Element> children = m.allOwnedElements();
		for (Element element : children) {
			if (element instanceof Class
					&& ((Class) element).allParents().contains(c))
				return (Class) element;
		}

		return null;
	}

	/**
	 * attributes for class and parent
	 * 
	 * @param cls
	 * @return
	 */
	public static EList<Property> getMineAndParentsAttributes(Class cls) {
		EList<Classifier> elems = cls.allParents();
		//cls.
		BasicEList<Property> list = new BasicEList();

		for (Classifier classifier : elems) {

			list.addAll(classifier.getAllAttributes());
		}

		list.addAll(cls.getAllAttributes());

		return list;
	}

	public static void setCurrentTemplateMode(Boolean currentTemplateMode) {
		ClassUtil.currentTemplateMode = currentTemplateMode;
	}

	public static Boolean isCurrentTemplateMode() {
		return currentTemplateMode;
	}

	public static void setModel(Model model) {
		ClassUtil.model = model;
	}

	public static Model getModel() {
		return model;
	}

	public static void setCurrentContainer(Boolean currentContainer) {
		ClassUtil.currentContainer = currentContainer;
	}

	public static Boolean getCurrentContainer() {
		return currentContainer;
	}

	/*
	 * public static List<Constraint> getAppliedConstraints(Parameter e){ e.
	 * .allOwnedElements().typeSelect(wcprofile::ValidationConstraint) }
	 */

}
