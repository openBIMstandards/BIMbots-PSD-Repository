package nl.tno.willemsph.psd_repository.xml;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import nl.tno.willemsph.psd_repository.common.LanguageTaggedStringInput;
import nl.tno.willemsph.psd_repository.property_definition.PropertyDefinitionInput;
import nl.tno.willemsph.psd_repository.property_set_definition.PropertySetDefinitionInput;
import nl.tno.willemsph.psd_repository.property_type.PropertyTypeInput;
import nl.tno.willemsph.psd_repository.sparql.EmbeddedServer;

public class PsetXmlResourceImporter {
	private final static Logger LOGGER = Logger.getLogger(PsetXmlResourceImporter.class.getName());

	private final static String PSD_CLASS_IFC_VERSION = EmbeddedServer.IFC4_PSD + "#IfcVersion";
	private final static String PSD_CLASS_PROPERTY_DEF = EmbeddedServer.IFC4_PSD + "#PropertyDef";
	private final static String PSD_CLASS_PROPERTY_SET_DEF = EmbeddedServer.IFC4_PSD + "#PropertySetDef";
	private final static String PSD_PROP_APPLICABLE_CLASS = EmbeddedServer.IFC4_PSD + "#applicableClass";
	private final static String PSD_PROP_APPLICABLE_TYPE_VALUE = EmbeddedServer.IFC4_PSD + "#applicableTypeValue";
	private final static String PSD_PROP_DATA_TYPE = EmbeddedServer.IFC4_PSD + "#dataType";
	private final static String PSD_PROP_DEFINED_VALUE = EmbeddedServer.IFC4_PSD + "#definedValue";
	private final static String PSD_PROP_DEFINING_VALUE = EmbeddedServer.IFC4_PSD + "#definingValue";
	private final static String PSD_PROP_DEFINITION = EmbeddedServer.IFC4_PSD + "#definition";
	private final static String PSD_PROP_DEFINITION_ALIAS = EmbeddedServer.IFC4_PSD + "#definitionAlias";
	private final static String PSD_PROP_ENUM_ITEM = EmbeddedServer.IFC4_PSD + "#enumItem";
	private final static String PSD_PROP_IFC_VERSION = EmbeddedServer.IFC4_PSD + "#ifcVersion";
	private final static String PSD_PROP_IFDGUID = EmbeddedServer.IFC4_PSD + "#ifdguid";
	private final static String PSD_PROP_NAME = EmbeddedServer.IFC4_PSD + "#name";
	private final static String PSD_PROP_NAME_ALIAS = EmbeddedServer.IFC4_PSD + "#nameAlias";
	private final static String PSD_PROP_PROPERTY_DEF = EmbeddedServer.IFC4_PSD + "#propertyDef";
	private final static String PSD_PROP_PROPERTY_TYPE = EmbeddedServer.IFC4_PSD + "#propertyType";
	private final static String PSD_PROP_REF_TYPE = EmbeddedServer.IFC4_PSD + "#reftype";
	private final static String PSD_PROP_VERSION = EmbeddedServer.IFC4_PSD + "#version";

	private URL psetXmlUri;

	private static PsetXmlResourceImporter instance;

	private PsetXmlResourceImporter() {
	}

	/**
	 * Singleton instance
	 * 
	 * @return instance
	 */
	public static PsetXmlResourceImporter getInstance() {
		if (instance == null) {
			instance = new PsetXmlResourceImporter();
		}
		return instance;
	}

	/**
	 * entry main function
	 * 
	 * @param args URL address Property Set Definition XML resource
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void main(String[] args)
			throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
		if (args.length == 1) {
			PropertySetDefinitionInput propertySetDefinitionInput = getInstance().importPsetXmlResource(args[0]);
			getInstance().generateRdfResource(propertySetDefinitionInput);
		} else {
			System.out.println("usage: PsetXmlResourceImporter <PsetXmlUri");
		}
	}

	/**
	 * Generate Turtle RDF file
	 * 
	 * @param pSetDefInput Property Set Definition input data.
	 */
	public void generateRdfResource(PropertySetDefinitionInput pSetDefInput) {
		Model defaultModel = ModelFactory.createDefaultModel();

		insertOntologyResource(defaultModel, pSetDefInput);
		insertPSetDefRsrc(defaultModel, pSetDefInput);

		defaultModel.write(System.out, "TURTLE", pSetDefInput.getId());
	}

	private void insertOntologyResource(Model model, PropertySetDefinitionInput pSetDefInput) {
		Resource pSetDefOntologyResrc = model.createResource(pSetDefInput.getId());
		model.setNsPrefix("", pSetDefInput.getId() + '#');
		model.setNsPrefix("rdf", RDF.uri);
		model.setNsPrefix("rdfs", RDFS.uri);
		model.setNsPrefix("owl", OWL.NS);
		model.setNsPrefix("IFC4", EmbeddedServer.IFC4 + '#');
		model.setNsPrefix("IFC4-PSD", EmbeddedServer.IFC4_PSD + '#');
		model.add(model.createStatement(pSetDefOntologyResrc, RDF.type, OWL.Ontology));
		model.add(model.createStatement(pSetDefOntologyResrc, OWL.imports,
				model.createResource(EmbeddedServer.IFC4_PSD)));
		model.add(model.createStatement(pSetDefOntologyResrc, OWL.versionInfo, "Created with BIM-Bots PSD Repository"));
	}

	private void insertPSetDefRsrc(Model model, PropertySetDefinitionInput pSetDefInput) {
		Resource pSetDefResrc = model.createResource(pSetDefInput.getId() + '#' + pSetDefInput.getName());
		model.add(model.createStatement(pSetDefResrc, RDF.type, model.createResource(PSD_CLASS_PROPERTY_SET_DEF)));
		for (String applicableClass : pSetDefInput.getApplicableClasses()) {
			model.add(model.createStatement(pSetDefResrc, model.createProperty(PSD_PROP_APPLICABLE_CLASS),
					model.createResource(applicableClass)));
		}
		// Applicable type value
		String applicableTypeValue = pSetDefInput.getApplicableTypeValue();
		if (applicableTypeValue != null) {
			model.add(model.createStatement(pSetDefResrc, model.createProperty(PSD_PROP_APPLICABLE_TYPE_VALUE),
					applicableTypeValue));
		}
		// Definition
		model.add(model.createStatement(pSetDefResrc, model.createProperty(PSD_PROP_DEFINITION),
				pSetDefInput.getDefinition()));
		// Definition alias
		List<LanguageTaggedStringInput> definitionAliases = pSetDefInput.getDefinitionAliases();
		if (definitionAliases != null) {
			for (LanguageTaggedStringInput definitionAlias : definitionAliases) {
				model.add(model.createStatement(pSetDefResrc, model.createProperty(PSD_PROP_DEFINITION_ALIAS),
						model.createLiteral(definitionAlias.getContent(), definitionAlias.getLanguage())));
			}
		}
		// IfcVersion
		Resource ifcVersion = model.createResource(new AnonId());
		model.add(model.createStatement(ifcVersion, RDF.type, model.createResource(PSD_CLASS_IFC_VERSION)));
		model.add(model.createStatement(ifcVersion, model.createProperty(PSD_PROP_VERSION),
				pSetDefInput.getIfcVersion()));
		model.add(model.createStatement(pSetDefResrc, model.createProperty(PSD_PROP_IFC_VERSION), ifcVersion));
		// Name
		model.add(model.createStatement(pSetDefResrc, model.createProperty(PSD_PROP_NAME), pSetDefInput.getName()));
		// PropertyDefs
		for (PropertyDefinitionInput pDefInput : pSetDefInput.getPropertyDefs()) {
			Resource pSetResrc = insertPDefRsrc(model, pSetDefResrc, pDefInput);
			model.add(model.createStatement(pSetDefResrc, model.createProperty(PSD_PROP_PROPERTY_DEF), pSetResrc));
		}
	}

	private Resource insertPDefRsrc(Model model, Resource pSetDefResrc, PropertyDefinitionInput pDefInput) {
		Resource pDefResrc = model.createResource(pDefInput.getId());
		model.add(model.createStatement(pDefResrc, RDF.type, model.createResource(PSD_CLASS_PROPERTY_DEF)));
		// Definition
		String definition = pDefInput.getDefinition();
		if (definition != null) {
			model.add(model.createStatement(pDefResrc, model.createProperty(PSD_PROP_DEFINITION), definition));
		}
		// Definition alias
		List<LanguageTaggedStringInput> definitionAliases = pDefInput.getDefinitionAliases();
		if (definitionAliases != null) {
			for (LanguageTaggedStringInput definitionAlias : definitionAliases) {
				model.add(model.createStatement(pDefResrc, model.createProperty(PSD_PROP_DEFINITION_ALIAS),
						model.createLiteral(definitionAlias.getContent(), definitionAlias.getLanguage())));
			}
		}
		// Ifdguid
		model.add(model.createStatement(pDefResrc, model.createProperty(PSD_PROP_IFDGUID), pDefInput.getIfdguid()));
		// Name
		model.add(model.createStatement(pDefResrc, model.createProperty(PSD_PROP_NAME), pDefInput.getName()));
		// Name alias
		List<LanguageTaggedStringInput> nameAliases = pDefInput.getNameAliases();
		if (nameAliases != null) {
			for (LanguageTaggedStringInput nameAlias : nameAliases) {
				model.add(model.createStatement(pDefResrc, model.createProperty(PSD_PROP_NAME_ALIAS),
						model.createLiteral(nameAlias.getContent(), nameAlias.getLanguage())));
			}
		}
		// PropertyType
		Resource propertyType = model.createResource(new AnonId());
		LOGGER.info(pDefInput.getPropertyType().getType().toString());
		model.add(model.createStatement(propertyType, RDF.type,
				model.createResource(pDefInput.getPropertyType().getType().toString())));
		if (pDefInput.getPropertyType().getDataType() != null) {
			model.add(model.createStatement(propertyType, model.createProperty(PSD_PROP_DATA_TYPE),
					model.createResource(pDefInput.getPropertyType().getDataType())));
		}
		if (pDefInput.getPropertyType().getReftype() != null) {
			model.add(model.createStatement(propertyType, model.createProperty(PSD_PROP_REF_TYPE),
					model.createResource(pDefInput.getPropertyType().getReftype())));
		}
		if (pDefInput.getPropertyType().getDefiningValue() != null) {
			model.add(model.createStatement(propertyType, model.createProperty(PSD_PROP_DEFINING_VALUE),
					model.createResource(pDefInput.getPropertyType().getDefiningValue())));
		}
		if (pDefInput.getPropertyType().getDefinedValue() != null) {
			model.add(model.createStatement(propertyType, model.createProperty(PSD_PROP_DEFINED_VALUE),
					model.createResource(pDefInput.getPropertyType().getDefinedValue())));
		}
		if (pDefInput.getPropertyType().getEnumItems() != null) {
			for (String enumItem : pDefInput.getPropertyType().getEnumItems()) {
				model.add(model.createStatement(propertyType, model.createProperty(PSD_PROP_ENUM_ITEM), enumItem));
			}
		}
		model.add(model.createStatement(pDefResrc, model.createProperty(PSD_PROP_PROPERTY_TYPE), propertyType));

		// In case of a IFC4-PSD property set
		if (pDefInput.getId().startsWith(EmbeddedServer.IFC4_PSD)) {
			Resource objectProperty = model.createResource(
					pDefInput.getId().substring(0, pDefInput.getId().indexOf('#') + 1) + pDefInput.getName());
			model.add(model.createStatement(objectProperty, RDF.type, RDF.Property));
			model.add(model.createStatement(objectProperty, RDFS.subPropertyOf,
					model.createResource(EmbeddedServer.IFC4_PSD + "#" + pDefInput.getName())));
			model.add(model.createStatement(objectProperty, RDFS.seeAlso, pDefResrc));
		}
		return pDefResrc;
	}

	/**
	 * Create property set definition input object
	 * 
	 * @param resource URL Property Set Definition XML resource.
	 * @return Property Set Definition input bean
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public PropertySetDefinitionInput importPsetXmlResource(String resource)
			throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
		// Set up XML parser
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Open URL connection
		psetXmlUri = new URL(resource);
		String resourceName = getResourceName(resource);
		HttpURLConnection con = (HttpURLConnection) psetXmlUri.openConnection();

		// Parse XML document
		Document psetdoc = builder.parse(con.getInputStream());
		Element documentElement = psetdoc.getDocumentElement();

		return createPropertySetDefinitionInput(documentElement, EmbeddedServer.IFC4_PSD + "/" + resourceName);
	}

	private PropertySetDefinitionInput createPropertySetDefinitionInput(Element documentElement, String id)
			throws DOMException, URISyntaxException {
		PropertySetDefinitionInput psetDefInput = new PropertySetDefinitionInput();
		psetDefInput.setId(id);

		List<Element> childElements = getChildElements(documentElement);

		for (Element childElement : childElements) {
			switch (childElement.getTagName()) {
			case "ApplicableClasses":
				List<String> applicableClasses = new ArrayList<>();
				List<Element> classNames = getChildElements(childElement);
				for (Element className : classNames) {
					applicableClasses.add(EmbeddedServer.IFC4 + "#" + className.getTextContent());
				}
				psetDefInput.setApplicableClasses(applicableClasses);
				break;
			case "ApplicableTypeValue":
				psetDefInput.setApplicableTypeValue(childElement.getTextContent());
				break;
			case "Definition":
				psetDefInput.setDefinition(childElement.getTextContent());
				break;
			case "PsetDefinitionAliases":
				List<Element> psetDefinitionAliasElements = getChildElements(childElement);
				for (Element psetDefinitionAliasElement : psetDefinitionAliasElements) {
					List<LanguageTaggedStringInput> definitionAliases = psetDefInput.getDefinitionAliases();
					if (definitionAliases == null) {
						definitionAliases = new ArrayList<>();
					}
					String lang = psetDefinitionAliasElement.getAttribute("lang");
					String textContent = psetDefinitionAliasElement.getTextContent();
					definitionAliases.add(new LanguageTaggedStringInput(lang, textContent));
					psetDefInput.setDefinitionAliases(definitionAliases);
				}
				break;
			case "IfcVersion":
				psetDefInput.setIfcVersion(childElement.getAttribute("version"));
				break;
			case "Name":
				psetDefInput.setName(childElement.getTextContent());
				break;
			case "PropertyDefs":
				List<PropertyDefinitionInput> propertyDefs = new ArrayList<>();
				List<Element> propertyDefElements = getChildElements(childElement);
				for (Element propertyDefElement : propertyDefElements) {
					String ifdguid = propertyDefElement.getAttribute("ifdguid");
					PropertyDefinitionInput propertyDefinitionInput = createPropertyDefinitionInput(propertyDefElement,
							id + "#p" + ifdguid);
					propertyDefs.add(propertyDefinitionInput);
				}
				psetDefInput.setPropertyDefs(propertyDefs);
				break;
			}
		}

		LOGGER.info("psetDefInput: " + psetDefInput);
		return psetDefInput;
	}

	private PropertyDefinitionInput createPropertyDefinitionInput(Element propertyDefElement, String id)
			throws URISyntaxException {
		PropertyDefinitionInput propDefInput = new PropertyDefinitionInput();
		propDefInput.setId(id);
		propDefInput.setIfdguid(propertyDefElement.getAttribute("ifdguid"));
		List<Element> childElements = getChildElements(propertyDefElement);
		for (Element childElement : childElements) {
			switch (childElement.getTagName()) {
			case "Definition":
				propDefInput.setDefinition(childElement.getTextContent());
				break;
			case "DefinitionAliases":
				List<Element> definitionAliasElements = getChildElements(childElement);
				for (Element definitionAliasElement : definitionAliasElements) {
					List<LanguageTaggedStringInput> definitionAliases = propDefInput.getDefinitionAliases();
					if (definitionAliases == null) {
						definitionAliases = new ArrayList<>();
					}
					String lang = definitionAliasElement.getAttribute("lang");
					String textContent = definitionAliasElement.getTextContent();
					definitionAliases.add(new LanguageTaggedStringInput(lang, textContent));
					propDefInput.setDefinitionAliases(definitionAliases);
				}
				break;
			case "Name":
				propDefInput.setName(childElement.getTextContent());
				break;
			case "NameAliases":
				List<Element> nameAliasElements = getChildElements(childElement);
				for (Element nameAliasElement : nameAliasElements) {
					List<LanguageTaggedStringInput> nameAliases = propDefInput.getNameAliases();
					if (nameAliases == null) {
						nameAliases = new ArrayList<>();
					}
					String lang = nameAliasElement.getAttribute("lang");
					String textContent = nameAliasElement.getTextContent();
					nameAliases.add(new LanguageTaggedStringInput(lang, textContent));
					propDefInput.setNameAliases(nameAliases);
				}
				break;
			case "PropertyType":
				PropertyTypeInput propertyType = new PropertyTypeInput();
				List<Element> propertyTypeElements = getChildElements(childElement);
				for (Element propertyTypeElement : propertyTypeElements) {
					propertyType.setType(EmbeddedServer.IFC4_PSD + "#" + propertyTypeElement.getTagName());
					switch (propertyTypeElement.getTagName()) {
					case "TypePropertyEnumeratedValue":
						List<Element> typePropertyEnumeratedValueElements = getChildElements(propertyTypeElement);
						for (Element typeElement : typePropertyEnumeratedValueElements) {
							switch (typeElement.getTagName()) {
							case "EnumList":
								List<String> enumItems = new ArrayList<>();
								List<Element> enumItemElements = getChildElements(typeElement);
								for (Element enumItemElement : enumItemElements) {
									enumItems.add(enumItemElement.getTextContent());
								}
								propertyType.setEnumItems(enumItems);
								break;
							case "ConstantList":
								List<String> constantDefs = new ArrayList<>();
								List<Element> constantDefElements = getChildElements(typeElement);
								for (Element constantDefElement : constantDefElements) {
									NodeList nameElements = constantDefElement.getElementsByTagName("Name");
									constantDefs.add(nameElements.item(0).getTextContent());
								}
								propertyType.setEnumItems(constantDefs);
								break;
							}
						}
						break;
					case "TypePropertyBoundedValue":
						List<Element> typePropertyBoundedValue = getChildElements(propertyTypeElement);
						for (Element typeElement : typePropertyBoundedValue) {
							switch (typeElement.getTagName()) {
							case "DataType":
								String typeAttribute = typeElement.getAttribute("type");
								if (typeAttribute != null && !typeAttribute.isEmpty()) {
									propertyType.setDataType(EmbeddedServer.IFC4 + '#' + typeAttribute);
								}
								break;
							}
						}
						break;
					case "TypePropertyListValue":
						List<Element> typePropertyListValueElements = getChildElements(propertyTypeElement);
						for (Element typeElement : typePropertyListValueElements) {
							switch (typeElement.getTagName()) {
							case "ListValue":
								List<Element> listValueList = getChildElements(typeElement);
								if (listValueList.size() == 1) {
									propertyType.setDataType(
											EmbeddedServer.IFC4 + '#' + listValueList.get(0).getAttribute("type"));
								}
								break;
							}
						}
						break;
					case "TypePropertyReferenceValue":
						String reftype = propertyTypeElement.getAttribute("reftype");
						if (reftype != null) {
							propertyType.setReftype(EmbeddedServer.IFC4 + "#" + reftype);
						}
						break;
					case "TypePropertySingleValue":
						List<Element> typePropertySingleValueElements = getChildElements(propertyTypeElement);
						for (Element typeElement : typePropertySingleValueElements) {
							switch (typeElement.getTagName()) {
							case "DataType":
								String typeAttribute = typeElement.getAttribute("type");
								if (typeAttribute != null && !typeAttribute.isEmpty()) {
									propertyType.setDataType(EmbeddedServer.IFC4 + '#' + typeAttribute);
								}
								break;
							}
						}
						break;
					case "TypePropertyTableValue":
						List<Element> typePropertyTableValue = getChildElements(propertyTypeElement);
						for (Element typeElement : typePropertyTableValue) {
							switch (typeElement.getTagName()) {
							case "Expression":
								break;
							case "DefiningValue":
								List<Element> definingValueList = getChildElements(typeElement);
								if (definingValueList.size() == 1) {
									propertyType.setDefiningValue(
											EmbeddedServer.IFC4 + '#' + definingValueList.get(0).getAttribute("type"));
								}
								break;
							case "DefinedValue":
								List<Element> definedValueList = getChildElements(typeElement);
								if (definedValueList.size() == 1) {
									propertyType.setDefinedValue(
											EmbeddedServer.IFC4 + '#' + definedValueList.get(0).getAttribute("type"));
								}
								break;
							}
						}
						break;
					}
				}
				propDefInput.setPropertyType(propertyType);
				break;
			}
		}
		return propDefInput;
	}

	private String getResourceName(String resource) {
		int lastIndexOfSlash = resource.lastIndexOf('/');
		int lastIndexOfXml = resource.lastIndexOf(".xml");
		return resource.substring(lastIndexOfSlash + 1, lastIndexOfXml);
	}

	private List<Element> getChildElements(Element parentElement) {
		List<Element> childElements = new ArrayList<>();
		NodeList childNodes = parentElement.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			Node node = childNodes.item(index);
			if (node instanceof Element) {
				Element element = (Element) node;
				childElements.add(element);
			}
		}
		return childElements;
	}

}
