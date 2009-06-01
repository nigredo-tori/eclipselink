package dbws.testing.visit;

//javase imports
import java.io.StringReader;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import org.w3c.dom.Document;

//JUnit4 imports
//import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

//EclipseLink imports
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.dynamicpersist.BaseEntity;
import org.eclipse.persistence.internal.dynamicpersist.BaseEntityAccessor;
import org.eclipse.persistence.internal.dynamicpersist.BaseEntityClassLoader;
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;
import org.eclipse.persistence.internal.oxm.schema.SchemaModelGenerator;
import org.eclipse.persistence.internal.oxm.schema.SchemaModelGeneratorProperties;
import org.eclipse.persistence.internal.oxm.schema.SchemaModelProject;
import org.eclipse.persistence.internal.oxm.schema.model.Schema;
//import org.eclipse.persistence.logging.SessionLog;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.structures.ObjectRelationalDataTypeDescriptor;
import org.eclipse.persistence.oxm.XMLContext;
import org.eclipse.persistence.oxm.XMLDescriptor;
import org.eclipse.persistence.oxm.XMLMarshaller;
import org.eclipse.persistence.platform.database.oracle.publisher.PublisherException;
import org.eclipse.persistence.platform.database.oracle.publisher.visit.PublisherListenerChainAdapter;
import org.eclipse.persistence.platform.database.oracle.publisher.visit.PublisherWalker;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ValueReadQuery;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.DatabaseSession;
import org.eclipse.persistence.sessions.Project;
import org.eclipse.persistence.tools.dbws.DBWSBuilder;
import org.eclipse.persistence.tools.dbws.OperationModel;
import org.eclipse.persistence.tools.dbws.ProcedureOperationModel;
import org.eclipse.persistence.tools.dbws.TypeSuffixTransformer;
import org.eclipse.persistence.tools.dbws.DBWSBuilder.DbStoredProcedureNameAndModel;
import org.eclipse.persistence.tools.dbws.jdbc.DbStoredProcedure;
import org.eclipse.persistence.tools.dbws.oracle.AdvancedJDBCORDescriptorBuilder;
import org.eclipse.persistence.tools.dbws.oracle.AdvancedJDBCOXDescriptorBuilder;
import org.eclipse.persistence.tools.dbws.oracle.AdvancedJDBCQueryBuilder;
import org.eclipse.persistence.tools.dbws.oracle.OracleHelper;
import static org.eclipse.persistence.internal.oxm.schema.SchemaModelGeneratorProperties.ELEMENT_FORM_QUALIFIED_KEY;

//domain (testing) imports
import static dbws.testing.visit.WebServiceTestSuite.DEFAULT_DATABASE_DRIVER;

public class AdvancedJDBCTestSuite extends BuilderTestSuite {
    
    static final String REGION_OR_PROJECT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>region</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.region</class>" +
             "<alias>region</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>reg_id</attribute-name>" +
                   "<field name=\"REG_ID\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>reg_name</attribute-name>" +
                   "<field name=\"REG_NAME\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>REGION</structure>" +
             "<field-order>" +
                "<field name=\"REG_ID\" xsi:type=\"column\"/>" +
                "<field name=\"REG_NAME\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
       "<queries>" +
           "<query name=\"echoRegion\" xsi:type=\"value-read-query\">" +
              "<arguments>" +
                 "<argument name=\"AREGION\">" +
                    "<type>java.lang.Object</type>" +
                 "</argument>" +
              "</arguments>" +
              "<maintain-cache>false</maintain-cache>" +
              "<bind-all-parameters>true</bind-all-parameters>" +
              "<call xsi:type=\"stored-function-call\">" +
                 "<procedure-name>ADVANCED_OBJECT_DEMO.ECHOREGION</procedure-name>" +
                 "<cursor-output-procedure>false</cursor-output-procedure>" +
                 "<arguments>" +
                    "<argument xsi:type=\"procedure-argument\">" +
                       "<procedure-argument-name>AREGION</procedure-argument-name>" +
                       "<argument-name>AREGION</argument-name>" +
                       "<procedure-argument-type>advanced_object_demo.region</procedure-argument-type>" +
                       "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                       "<procedure-argument-sqltype-name>REGION</procedure-argument-sqltype-name>" +
                    "</argument>" +
                 "</arguments>" +
                 "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                    "<procedure-argument-type>advanced_object_demo.region</procedure-argument-type>" +
                    "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                    "<procedure-argument-sqltype-name>REGION</procedure-argument-sqltype-name>" +
                 "</stored-function-result>" +
              "</call>" +
           "</query>" +
       "</queries>" +
    "</object-persistence>";
    @SuppressWarnings("unchecked")
    @Test
    public void struct1LevelDeep_OrPart() 
        throws SQLException, PublisherException, InstantiationException, IllegalAccessException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("echoRegion");
        pModel.setCatalogPattern("ADVANCED_OBJECT_DEMO");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("ECHOREGION");
        pModel.setReturnType("regionType");
        testOrProject(pModel, "region", REGION_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(REGION_OR_PROJECT);
        Class regionClass = ds.getProject().getDescriptorForAlias("region").getJavaClass();
        ValueReadQuery vrq = (ValueReadQuery)ds.getQuery("echoRegion");
        BaseEntity regionEntity = (BaseEntity)regionClass.newInstance();
        regionEntity.set(0, BigDecimal.valueOf(5));
        regionEntity.set(1, "this is a test");
        Vector v = new NonSynchronizedVector();
        v.add(regionEntity);
        Object o = ds.executeQuery(vrq, v);
        assertTrue("incorrect return type from StoredFunctionCall",
            o instanceof BaseEntity);
        BaseEntity regionEntityEchoed = (BaseEntity)o;
        assertTrue("incorrect first field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(0).equals(BigDecimal.valueOf(5)));
        assertTrue("incorrect second field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(1).equals("this is a test"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void struct1LevelDeep_OxPart() throws SQLException, PublisherException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setCatalogPattern("advanced_object_demo");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("echoRegion");
        testOxProject(pModel, "region", "urn:struct1", REGION_OX_PROJECT, REGION_SCHEMA);
    }

    static final String REGION_OX_PROJECT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>region</name>" +
       "<class-mapping-descriptors>" +
           "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
               "<class>advanced_object_demo.region</class>" +
               "<alias>region</alias>" +
               "<events xsi:type=\"event-policy\"/>" +
               "<querying xsi:type=\"query-policy\"/>" +
               "<attribute-mappings>" +
                  "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                     "<attribute-name>reg_id</attribute-name>" +
                     "<field name=\"reg_id/text()\" xsi:type=\"node\">" +
                        "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                     "</field>" +
                     "<attribute-classification>java.math.BigDecimal</attribute-classification>" +
                  "</attribute-mapping>" +
                  "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                     "<attribute-name>reg_name</attribute-name>" +
                     "<field name=\"reg_name/text()\" xsi:type=\"node\">" +
                         "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                     "</field>" +
                     "<attribute-classification>java.lang.String</attribute-classification>" +
                  "</attribute-mapping>" +
               "</attribute-mappings>" +
               "<descriptor-type>aggregate</descriptor-type>" +
               "<instantiation/>" +
               "<copying xsi:type=\"instantiation-copy-policy\"/>" +
               "<default-root-element>regionType</default-root-element>" +
               "<default-root-element-field name=\"regionType\" xsi:type=\"node\"/>" +
               "<namespace-resolver>" +
                  "<default-namespace-uri>urn:struct1</default-namespace-uri>" +
               "</namespace-resolver>" +
               "<schema xsi:type=\"schema-url-reference\">" +
                  "<schema-context>/regionType</schema-context>" +
                  "<node-type>complex-type</node-type>" +
               "</schema>" +
           "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
    "</object-persistence>";
    static final String REGION_SCHEMA =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<xsd:schema targetNamespace=\"urn:struct1\" xmlns=\"urn:struct1\" elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
        "<xsd:complexType name=\"regionType\">" +
           "<xsd:sequence>" +
              "<xsd:element name=\"reg_id\" type=\"xsd:decimal\" minOccurs=\"0\"/>" +
              "<xsd:element name=\"reg_name\" type=\"xsd:string\" minOccurs=\"0\"/>" +
           "</xsd:sequence>" +
        "</xsd:complexType>" +
        "<xsd:element name=\"regionType\" type=\"regionType\"/>" +
    "</xsd:schema>";
    static final String EMPADDRESS_OR_PROJECT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>empAddress</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.region</class>" +
             "<alias>region</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>reg_id</attribute-name>" +
                   "<field name=\"REG_ID\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>reg_name</attribute-name>" +
                   "<field name=\"REG_NAME\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>REGION</structure>" +
             "<field-order>" +
                "<field name=\"REG_ID\" xsi:type=\"column\"/>" +
                "<field name=\"REG_NAME\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.emp_address</class>" +
             "<alias>emp_address</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>street</attribute-name>" +
                   "<field name=\"STREET\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>suburb</attribute-name>" +
                   "<field name=\"SUBURB\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"structure-mapping\">" +
                   "<attribute-name>addr_region</attribute-name>" +
                   "<reference-class>advanced_object_demo.region</reference-class>" +
                   "<field name=\"ADDR_REGION\" xsi:type=\"object-relational-field\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>postcode</attribute-name>" +
                   "<field name=\"POSTCODE\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>EMP_ADDRESS</structure>" +
             "<field-order>" +
                "<field name=\"STREET\" xsi:type=\"column\"/>" +
                "<field name=\"SUBURB\" xsi:type=\"column\"/>" +
                "<field name=\"ADDR_REGION\" xsi:type=\"column\"/>" +
                "<field name=\"POSTCODE\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
       "<queries>" +
          "<query name=\"echoEmpAddress\" xsi:type=\"value-read-query\">" +
             "<arguments>" +
                "<argument name=\"ANEMPADDRESS\">" +
                   "<type>java.lang.Object</type>" +
                "</argument>" +
             "</arguments>" +
             "<maintain-cache>false</maintain-cache>" +
             "<bind-all-parameters>true</bind-all-parameters>" +
             "<call xsi:type=\"stored-function-call\">" +
                "<procedure-name>advanced_object_demo.ECHOEMPADDRESS</procedure-name>" +
                "<cursor-output-procedure>false</cursor-output-procedure>" +
                "<arguments>" +
                   "<argument xsi:type=\"procedure-argument\">" +
                      "<procedure-argument-name>ANEMPADDRESS</procedure-argument-name>" +
                      "<argument-name>ANEMPADDRESS</argument-name>" +
                      "<procedure-argument-type>advanced_object_demo.emp_address</procedure-argument-type>" +
                      "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                      "<procedure-argument-sqltype-name>EMP_ADDRESS</procedure-argument-sqltype-name>" +
                   "</argument>" +
                "</arguments>" +
                "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                   "<procedure-argument-type>advanced_object_demo.emp_address</procedure-argument-type>" +
                   "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                   "<procedure-argument-sqltype-name>EMP_ADDRESS</procedure-argument-sqltype-name>" +
                "</stored-function-result>" +
             "</call>" +
          "</query>" +
       "</queries>" +
    "</object-persistence>";
    @SuppressWarnings("unchecked")
    @Test
    public void struct2LevelDeep_OrPart()
        throws SQLException, PublisherException, InstantiationException, IllegalAccessException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("echoEmpAddress");
        pModel.setCatalogPattern("advanced_object_demo"); // test case-insensitivity
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("echoEmpAddress");
        pModel.setReturnType("empAddressType");
        testOrProject(pModel, "empAddress", EMPADDRESS_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(EMPADDRESS_OR_PROJECT);
        ObjectRelationalDataTypeDescriptor regionDesc = 
            (ObjectRelationalDataTypeDescriptor)ds.getProject().getDescriptorForAlias("region");
        Class regionClass = regionDesc.getJavaClass();
        ObjectRelationalDataTypeDescriptor empAddressDesc = 
            (ObjectRelationalDataTypeDescriptor)ds.getProject().getDescriptorForAlias("emp_address");
        Class empAddressClass = empAddressDesc.getJavaClass();
        ValueReadQuery vrq = (ValueReadQuery)ds.getQuery("echoEmpAddress");
        BaseEntity regionEntity = (BaseEntity)regionClass.newInstance();
        regionEntity.set(0, BigDecimal.valueOf(5));
        regionEntity.set(1, "this is a test");
        BaseEntity empAddressEntity = (BaseEntity)empAddressClass.newInstance();
        empAddressEntity.set(0, "20 Pinetrail Cres.");
        empAddressEntity.set(1, "Centrepointe");
        empAddressEntity.set(2, regionEntity);
        empAddressEntity.set(3, BigDecimal.valueOf(12));
        Vector v = new NonSynchronizedVector();
        v.add(empAddressEntity);
        Object o = ds.executeQuery(vrq, v);
        assertTrue("incorect return type from StoredFunctionCall", o instanceof BaseEntity);
        BaseEntity addressEntityEchoed = (BaseEntity)o;
        assertTrue("incorrect first field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(0).equals("20 Pinetrail Cres."));
        assertTrue("incorrect second field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(1).equals("Centrepointe"));
        BaseEntity regionEntityEchoed = (BaseEntity)addressEntityEchoed.get(2);
        assertTrue("incorrect nested-third-first field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(0).equals(BigDecimal.valueOf(5)));
        assertTrue("incorrect nested-third-second field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(1).equals("this is a test"));
        assertTrue("incorrect fourth field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(3).equals(BigDecimal.valueOf(12)));
    }
    @SuppressWarnings("unchecked")
    @Test
    public void struct2LevelDeep_OxPart() throws SQLException, PublisherException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setCatalogPattern("advanced_object_demo");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("echoEmpAddress");
        testOxProject(pModel, "empAddress", "urn:struct2", EMPADDRESS_OX_PROJECT, EMPADDRESS_SCHEMA);
    }
    static final String EMPADDRESS_OX_PROJECT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>empAddress</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.region</class>" +
             "<alias>region</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                   "<attribute-name>reg_id</attribute-name>" +
                   "<field name=\"reg_id/text()\" xsi:type=\"node\">" +
                      "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                   "</field>" +
                   "<attribute-classification>java.math.BigDecimal</attribute-classification>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                   "<attribute-name>reg_name</attribute-name>" +
                   "<field name=\"reg_name/text()\" xsi:type=\"node\">" +
                       "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                    "</field>" +
                    "<attribute-classification>java.lang.String</attribute-classification>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<namespace-resolver>" +
                "<default-namespace-uri>urn:struct2</default-namespace-uri>" +
             "</namespace-resolver>" +
             "<schema xsi:type=\"schema-url-reference\">" +
                "<schema-context>/regionType</schema-context>" +
                "<node-type>complex-type</node-type>" +
             "</schema>" +
          "</class-mapping-descriptor>" +
          "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.emp_address</class>" +
             "<alias>emp_address</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
               "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                  "<attribute-name>street</attribute-name>" +
                  "<field name=\"street/text()\" xsi:type=\"node\">" +
                     "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                  "</field>" +
                  "<attribute-classification>java.lang.String</attribute-classification>" +
               "</attribute-mapping>" +
               "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                  "<attribute-name>suburb</attribute-name>" +
                  "<field name=\"suburb/text()\" xsi:type=\"node\">" +
                     "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                  "</field>" +
                  "<attribute-classification>java.lang.String</attribute-classification>" +
               "</attribute-mapping>" +
               "<attribute-mapping xsi:type=\"xml-composite-object-mapping\">" +
                  "<attribute-name>addr_region</attribute-name>" +
                  "<reference-class>advanced_object_demo.region</reference-class>" +
                  "<field name=\"addr_region\" xsi:type=\"node\"/>" +
               "</attribute-mapping>" +
               "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                  "<attribute-name>postcode</attribute-name>" +
                  "<field name=\"postcode/text()\" xsi:type=\"node\">" +
                     "<schema-type>{http://www.w3.org/2001/XMLSchema}integer</schema-type>" +
                  "</field>" +
                  "<attribute-classification>java.math.BigInteger</attribute-classification>" +
               "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<default-root-element>emp_addressType</default-root-element>" +
             "<default-root-element-field name=\"emp_addressType\" xsi:type=\"node\"/>" +
             "<namespace-resolver>" +
                "<default-namespace-uri>urn:struct2</default-namespace-uri>" +
             "</namespace-resolver>" +
             "<schema xsi:type=\"schema-url-reference\">" +
                "<schema-context>/emp_addressType</schema-context>" +
                "<node-type>complex-type</node-type>" +
             "</schema>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
    "</object-persistence>";
    static final String EMPADDRESS_SCHEMA =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<xsd:schema targetNamespace=\"urn:struct2\" xmlns=\"urn:struct2\" elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
       "<xsd:complexType name=\"emp_addressType\">" +
          "<xsd:sequence>" +
             "<xsd:element name=\"street\" type=\"xsd:string\" minOccurs=\"0\"/>" +
             "<xsd:element name=\"suburb\" type=\"xsd:string\" minOccurs=\"0\"/>" +
             "<xsd:element name=\"addr_region\" type=\"regionType\" minOccurs=\"0\"/>" +
             "<xsd:element name=\"postcode\" type=\"xsd:integer\" minOccurs=\"0\"/>" +
          "</xsd:sequence>" +
       "</xsd:complexType>" +
       "<xsd:complexType name=\"regionType\">" +
          "<xsd:sequence>" +
             "<xsd:element name=\"reg_id\" type=\"xsd:decimal\" minOccurs=\"0\"/>" +
             "<xsd:element name=\"reg_name\" type=\"xsd:string\" minOccurs=\"0\"/>" +
          "</xsd:sequence>" +
       "</xsd:complexType>" +
       "<xsd:element name=\"emp_addressType\" type=\"emp_addressType\"/>" +
    "</xsd:schema>";
    static final String EMPOBJECT_OR_PROJECT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>empObject</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.region</class>" +
             "<alias>region</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>reg_id</attribute-name>" +
                   "<field name=\"REG_ID\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>reg_name</attribute-name>" +
                   "<field name=\"REG_NAME\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>REGION</structure>" +
             "<field-order>" +
                "<field name=\"REG_ID\" xsi:type=\"column\"/>" +
                "<field name=\"REG_NAME\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.emp_object</class>" +
             "<alias>emp_object</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>employee_id</attribute-name>" +
                   "<field name=\"EMPLOYEE_ID\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"structure-mapping\">" +
                   "<attribute-name>address</attribute-name>" +
                   "<reference-class>advanced_object_demo.emp_address</reference-class>" +
                   "<field name=\"ADDRESS\" xsi:type=\"object-relational-field\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>employee_name</attribute-name>" +
                   "<field name=\"EMPLOYEE_NAME\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>date_of_hire</attribute-name>" +
                   "<field name=\"DATE_OF_HIRE\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>EMP_OBJECT</structure>" +
             "<field-order>" +
                "<field name=\"EMPLOYEE_ID\" xsi:type=\"column\"/>" +
                "<field name=\"ADDRESS\" xsi:type=\"column\"/>" +
                "<field name=\"EMPLOYEE_NAME\" xsi:type=\"column\"/>" +
                "<field name=\"DATE_OF_HIRE\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>advanced_object_demo.emp_address</class>" +
             "<alias>emp_address</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>street</attribute-name>" +
                   "<field name=\"STREET\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>suburb</attribute-name>" +
                   "<field name=\"SUBURB\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"structure-mapping\">" +
                   "<attribute-name>addr_region</attribute-name>" +
                   "<reference-class>advanced_object_demo.region</reference-class>" +
                   "<field name=\"ADDR_REGION\" xsi:type=\"object-relational-field\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>postcode</attribute-name>" +
                   "<field name=\"POSTCODE\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>EMP_ADDRESS</structure>" +
             "<field-order>" +
                "<field name=\"STREET\" xsi:type=\"column\"/>" +
                "<field name=\"SUBURB\" xsi:type=\"column\"/>" +
                "<field name=\"ADDR_REGION\" xsi:type=\"column\"/>" +
                "<field name=\"POSTCODE\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
       "<queries>" +
          "<query name=\"echoEmpObject\" xsi:type=\"value-read-query\">" +
             "<arguments>" +
                "<argument name=\"ANEMPOBJECT\">" +
                   "<type>java.lang.Object</type>" +
                "</argument>" +
             "</arguments>" +
             "<maintain-cache>false</maintain-cache>" +
             "<bind-all-parameters>true</bind-all-parameters>" +
             "<call xsi:type=\"stored-function-call\">" +
                "<procedure-name>advanced_object_demo.ECHOEMPOBJECT</procedure-name>" +
                "<cursor-output-procedure>false</cursor-output-procedure>" +
                "<arguments>" +
                   "<argument xsi:type=\"procedure-argument\">" +
                      "<procedure-argument-name>ANEMPOBJECT</procedure-argument-name>" +
                      "<argument-name>ANEMPOBJECT</argument-name>" +
                      "<procedure-argument-type>advanced_object_demo.emp_object</procedure-argument-type>" +
                      "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                      "<procedure-argument-sqltype-name>EMP_OBJECT</procedure-argument-sqltype-name>" +
                   "</argument>" +
                "</arguments>" +
                "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                   "<procedure-argument-type>advanced_object_demo.emp_object</procedure-argument-type>" +
                   "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                   "<procedure-argument-sqltype-name>EMP_OBJECT</procedure-argument-sqltype-name>" +
                "</stored-function-result>" +
             "</call>" +
          "</query>" +
       "</queries>" +
    "</object-persistence>";
    @SuppressWarnings("unchecked")
    @Test
    public void struct3LevelDeep_OrPart()
        throws SQLException, PublisherException, InstantiationException, IllegalAccessException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("echoEmpObject");
        pModel.setCatalogPattern("advanced_object_demo"); // test case-insensitivity
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("echoEmpObject");
        pModel.setReturnType("empObject");
        testOrProject(pModel, "empObject", EMPOBJECT_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(EMPOBJECT_OR_PROJECT);
        ObjectRelationalDataTypeDescriptor regionDesc = 
            (ObjectRelationalDataTypeDescriptor)ds.getProject().getDescriptorForAlias("region");
        Class regionClass = regionDesc.getJavaClass();
        ObjectRelationalDataTypeDescriptor empAddressDesc = 
            (ObjectRelationalDataTypeDescriptor)ds.getProject().getDescriptorForAlias("emp_address");
        Class empAddressClass = empAddressDesc.getJavaClass();
        ObjectRelationalDataTypeDescriptor empObjectDesc = 
            (ObjectRelationalDataTypeDescriptor)ds.getProject().getDescriptorForAlias("emp_object");
        Class empObjectClass = empObjectDesc.getJavaClass();
        ValueReadQuery vrq = (ValueReadQuery)ds.getQuery("echoEmpObject");
        BaseEntity regionEntity = (BaseEntity)regionClass.newInstance();
        regionEntity.set(0, BigDecimal.valueOf(5));
        regionEntity.set(1, "this is a test");
        BaseEntity empAddressEntity = (BaseEntity)empAddressClass.newInstance();
        empAddressEntity.set(0, "20 Pinetrail Cres.");
        empAddressEntity.set(1, "Centrepointe");
        empAddressEntity.set(2, regionEntity);
        empAddressEntity.set(3, BigDecimal.valueOf(12));
        BaseEntity empObjectEntity = (BaseEntity)empObjectClass.newInstance();
        empObjectEntity.set(0, BigDecimal.valueOf(55));
        empObjectEntity.set(1, empAddressEntity);
        empObjectEntity.set(2, "Mike Norman");
        empObjectEntity.set(3, new java.sql.Date(System.currentTimeMillis()));
        Vector v = new NonSynchronizedVector();
        v.add(empObjectEntity);
        Object o = ds.executeQuery(vrq, v);
        assertTrue("incorect return type from StoredFunctionCall", o instanceof BaseEntity);
        BaseEntity empObjectEntityEchoed = (BaseEntity)o;
        assertTrue("incorrect first field for type returned from StoredFunctionCall",
            empObjectEntityEchoed.get(0).equals(BigDecimal.valueOf(55)));
        BaseEntity addressEntityEchoed = (BaseEntity)empObjectEntityEchoed.get(1);
        assertTrue("incorrect nested-second-first field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(0).equals("20 Pinetrail Cres."));
        assertTrue("incorrect nested-second-second field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(1).equals("Centrepointe"));
        BaseEntity regionEntityEchoed = (BaseEntity)addressEntityEchoed.get(2);
        assertTrue("incorrect nested-second-third-first field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(0).equals(BigDecimal.valueOf(5)));
        assertTrue("incorrect nested-second-third-second field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(1).equals("this is a test"));
        assertTrue("incorrect nested-second-fourth field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(3).equals(BigDecimal.valueOf(12)));
        assertTrue("incorrect third field for type returned from StoredFunctionCall",
            empObjectEntityEchoed.get(2).equals("Mike Norman"));
        // assume date works out
    }
    @SuppressWarnings("unchecked")
    @Test
    public void struct3LevelDeep_OxPart() throws SQLException, PublisherException,
        InstantiationException, IllegalAccessException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setCatalogPattern("advanced_object_demo");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("echoEmpObject");
        testOxProject(pModel, "empObject", "urn:struct3", EMPOBJECT_OX_PROJECT, EMPOBJECT_SCHEMA);
        // test marshalling
        BaseEntityClassLoader becl = new BaseEntityClassLoader(this.getClass().getClassLoader());
        XMLContext xmlContext = new XMLContext(readObjectPersistenceProject, becl);
        Project p2 = (Project)xmlContext.createUnmarshaller().unmarshal(
            new StringReader(EMPOBJECT_OX_PROJECT));
        for (Iterator i = p2.getDescriptors().values().iterator(); i.hasNext();) {
            ClassDescriptor desc = (ClassDescriptor) i.next();
            if (!BaseEntity.class.isAssignableFrom(desc.getJavaClass())) {
                continue;
            }
            int idx = 0;
            for (Iterator j = desc.getMappings().iterator(); j.hasNext();) {
                DatabaseMapping dm = (DatabaseMapping) j.next();
                String attributeName = dm.getAttributeName();
                dm.setAttributeAccessor(new BaseEntityAccessor(attributeName, idx++));
            }
            try {
                Class clz = desc.getJavaClass();
                Method setNumAttrs = clz.getMethod("setNumAttributes", Integer.class);
                setNumAttrs.invoke(clz, new Integer(idx));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // turn-off dynamic class generation
        becl.dontGenerateSubclasses();
        XMLDescriptor regionDesc = (XMLDescriptor)p2.getDescriptorForAlias("region");
        Class regionClass = regionDesc.getJavaClass();
        XMLDescriptor empAddressDesc = (XMLDescriptor)p2.getDescriptorForAlias("emp_address");
        Class empAddressClass = empAddressDesc.getJavaClass();
        XMLDescriptor empObjectDesc = (XMLDescriptor)p2.getDescriptorForAlias("emp_object");
        Class empObjectClass = empObjectDesc.getJavaClass();
        BaseEntity regionEntity = (BaseEntity)regionClass.newInstance();
        regionEntity.set(0, BigDecimal.valueOf(5));
        regionEntity.set(1, "this is a test");
        BaseEntity empAddressEntity = (BaseEntity)empAddressClass.newInstance();
        empAddressEntity.set(0, "20 Pinetrail Cres.");
        empAddressEntity.set(1, "Centrepointe");
        empAddressEntity.set(2, regionEntity);
        empAddressEntity.set(3, BigInteger.valueOf(12));
        BaseEntity empObjectEntity = (BaseEntity)empObjectClass.newInstance();
        empObjectEntity.set(0, BigDecimal.valueOf(55));
        empObjectEntity.set(1, empAddressEntity);
        empObjectEntity.set(2, "Mike Norman");
        java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
        empObjectEntity.set(3, today);
        XMLContext xmlContext2 = new XMLContext(p2, becl);
        Document empObjectEntityDoc = xmlPlatform.createDocument();
        xmlContext2.createMarshaller().marshal(empObjectEntity, empObjectEntityDoc);
        String empObjectEntityString = 
            DBWSTestHelper.documentToString(empObjectEntityDoc).replaceAll("[\r\n]", "");
        String anEmpObject = ANEMPOBJECT + today.toString() + ANEMPOBJECT_SUFFIX;
        assertTrue("instance empObject not same as control empObject",
            anEmpObject.equals(empObjectEntityString));
        // test un-marshalling
        BaseEntity echoedEmpObjectEntity = (BaseEntity)xmlContext2.createUnmarshaller().unmarshal(
            new StringReader(anEmpObject), empObjectClass);
        assertTrue("incorrect first field for type returned from StoredFunctionCall",
            echoedEmpObjectEntity.get(0).equals(BigDecimal.valueOf(55)));
        BaseEntity addressEntityEchoed = (BaseEntity)echoedEmpObjectEntity.get(1);
        assertTrue("incorrect nested-second-first field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(0).equals("20 Pinetrail Cres."));
        assertTrue("incorrect nested-second-second field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(1).equals("Centrepointe"));
        BaseEntity regionEntityEchoed = (BaseEntity)addressEntityEchoed.get(2);
        assertTrue("incorrect nested-second-third-first field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(0).equals(BigDecimal.valueOf(5)));
        assertTrue("incorrect nested-second-third-second field for type returned from StoredFunctionCall",
            regionEntityEchoed.get(1).equals("this is a test"));
        assertTrue("incorrect nested-second-fourth field for type returned from StoredFunctionCall",
            addressEntityEchoed.get(3).equals(BigInteger.valueOf(12)));
        assertTrue("incorrect third field for type returned from StoredFunctionCall",
            echoedEmpObjectEntity.get(2).equals("Mike Norman"));
        Object date = echoedEmpObjectEntity.get(3);
        assertTrue("incorrect fourth field (type) for type returned from StoredFunctionCall",
            date instanceof java.sql.Date);
    }
    static final String EMPOBJECT_OX_PROJECT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
           "<name>empObject</name>" +
           "<class-mapping-descriptors>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>advanced_object_demo.region</class>" +
                 "<alias>region</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>reg_id</attribute-name>" +
                       "<field name=\"reg_id/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.math.BigDecimal</attribute-classification>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>reg_name</attribute-name>" +
                       "<field name=\"reg_name/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.lang.String</attribute-classification>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:struct3</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/regionType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>advanced_object_demo.emp_object</class>" +
                 "<alias>emp_object</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>employee_id</attribute-name>" +
                       "<field name=\"employee_id/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.math.BigDecimal</attribute-classification>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-composite-object-mapping\">" +
                       "<attribute-name>address</attribute-name>" +
                       "<reference-class>advanced_object_demo.emp_address</reference-class>" +
                       "<field name=\"address\" xsi:type=\"node\"/>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>employee_name</attribute-name>" +
                       "<field name=\"employee_name/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.lang.String</attribute-classification>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>date_of_hire</attribute-name>" +
                       "<field name=\"date_of_hire/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}date</schema-type>" +
                          "<xml-to-java-conversion-pair>" +
                              "<qname>{http://www.w3.org/2001/XMLSchema}date</qname>" +
                              "<class-name>java.sql.Date</class-name>" +
                          "</xml-to-java-conversion-pair>" +
                          "<java-to-xml-conversion-pair>" +
                              "<qname>{http://www.w3.org/2001/XMLSchema}date</qname>" +
                              "<class-name>java.sql.Date</class-name>" +
                          "</java-to-xml-conversion-pair>" +
                       "</field>" +
                       "<attribute-classification>java.sql.Date</attribute-classification>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<default-root-element>emp_objectType</default-root-element>" +
                 "<default-root-element-field name=\"emp_objectType\" xsi:type=\"node\"/>" +
                 "<namespace-resolver>" +
                    "<namespaces>" +
                       "<namespace>" +
                          "<prefix>xsd</prefix>" +
                          "<namespace-uri>http://www.w3.org/2001/XMLSchema</namespace-uri>" +
                       "</namespace>" +
                       "<namespace>" +
                          "<prefix>xsi</prefix>" +
                          "<namespace-uri>http://www.w3.org/2001/XMLSchema-instance</namespace-uri>" +
                       "</namespace>" +
                    "</namespaces>" +
                    "<default-namespace-uri>urn:struct3</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/emp_objectType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>advanced_object_demo.emp_address</class>" +
                 "<alias>emp_address</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>street</attribute-name>" +
                       "<field name=\"street/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.lang.String</attribute-classification>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>suburb</attribute-name>" +
                       "<field name=\"suburb/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.lang.String</attribute-classification>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-composite-object-mapping\">" +
                       "<attribute-name>addr_region</attribute-name>" +
                       "<reference-class>advanced_object_demo.region</reference-class>" +
                       "<field name=\"addr_region\" xsi:type=\"node\"/>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>postcode</attribute-name>" +
                       "<field name=\"postcode/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}integer</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.math.BigInteger</attribute-classification>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:struct3</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/emp_addressType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
           "</class-mapping-descriptors>" +
        "</object-persistence>";
    static final String EMPOBJECT_SCHEMA =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xsd:schema targetNamespace=\"urn:struct3\" xmlns=\"urn:struct3\" elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
           "<xsd:complexType name=\"emp_addressType\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"street\" type=\"xsd:string\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"suburb\" type=\"xsd:string\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"addr_region\" type=\"regionType\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"postcode\" type=\"xsd:integer\" minOccurs=\"0\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"emp_objectType\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"employee_id\" type=\"xsd:decimal\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"address\" type=\"emp_addressType\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"employee_name\" type=\"xsd:string\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"date_of_hire\" type=\"xsd:date\" minOccurs=\"0\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"regionType\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"reg_id\" type=\"xsd:decimal\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"reg_name\" type=\"xsd:string\" minOccurs=\"0\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:element name=\"emp_objectType\" type=\"emp_objectType\"/>" +
        "</xsd:schema>";

    public static final String EMP_ARRAY_OR_PROJECT =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>empArray</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>another_advanced_demo.emp_info</class>" +
             "<alias>emp_info</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>id</attribute-name>" +
                   "<field name=\"ID\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>name</attribute-name>" +
                   "<field name=\"NAME\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>EMP_INFO</structure>" +
             "<field-order>" +
                "<field name=\"ID\" xsi:type=\"column\"/>" +
                "<field name=\"NAME\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>another_advanced_demo.emp_info_array_CollectionWrapper</class>" +
             "<alias>emp_info_array</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"object-array-mapping\">" +
                   "<attribute-name>items</attribute-name>" +
                   "<reference-class>another_advanced_demo.emp_info</reference-class>" +
                   "<field name=\"items\" xsi:type=\"object-relational-field\"/>" +
                   "<container xsi:type=\"list-container-policy\">" +
                      "<collection-type>java.util.ArrayList</collection-type>" +
                   "</container>" +
                   "<structure>EMP_INFO_ARRAY</structure>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
       "<queries>" +
          "<query name=\"buildEmpArray\" xsi:type=\"value-read-query\">" +
             "<arguments>" +
                "<argument name=\"NUM\">" +
                   "<type>java.lang.Object</type>" +
                "</argument>" +
             "</arguments>" +
             "<maintain-cache>false</maintain-cache>" +
             "<bind-all-parameters>true</bind-all-parameters>" +
             "<call xsi:type=\"stored-function-call\">" +
                "<procedure-name>another_advanced_demo.BUILDEMPARRAY</procedure-name>" +
                "<cursor-output-procedure>false</cursor-output-procedure>" +
                "<arguments>" +
                   "<argument xsi:type=\"procedure-argument\">" +
                      "<procedure-argument-name>NUM</procedure-argument-name>" +
                      "<argument-name>NUM</argument-name>" +
                   "</argument>" +
                "</arguments>" +
                "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                   "<procedure-argument-type>another_advanced_demo.emp_info_array_CollectionWrapper</procedure-argument-type>" +
                   "<procedure-argument-sqltype>2003</procedure-argument-sqltype>" +
                   "<procedure-argument-sqltype-name>EMP_INFO_ARRAY</procedure-argument-sqltype-name>" +
                   "<nested-type-field xsi:type=\"procedure-argument\">" +
                      "<procedure-argument-type>another_advanced_demo.emp_info</procedure-argument-type>" +
                      "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                      "<procedure-argument-sqltype-name>EMP_INFO</procedure-argument-sqltype-name>" +
                   "</nested-type-field>" +
                "</stored-function-result>" +
             "</call>" +
          "</query>" +
       "</queries>" +
    "</object-persistence>";
    static final String ANEMPOBJECT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
        "<emp_objectType xmlns=\"urn:struct3\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
           "<employee_id>55</employee_id>" +
           "<address>" +
              "<street>20 Pinetrail Cres.</street>" +
              "<suburb>Centrepointe</suburb>" +
              "<addr_region>" +
                 "<reg_id>5</reg_id>" +
                 "<reg_name>this is a test</reg_name>" +
              "</addr_region>" +
              "<postcode>12</postcode>" +
           "</address>" +
           "<employee_name>Mike Norman</employee_name>" +
           "<date_of_hire>";
    static final String ANEMPOBJECT_SUFFIX =
           "</date_of_hire>" +
       "</emp_objectType>";

    @SuppressWarnings("unchecked")
    @Test
    public void buildEmpArray_OrPart() throws SQLException, PublisherException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("buildEmpArray");
        pModel.setCatalogPattern("another_advanced_demo");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("buildEmpArray");
        pModel.setReturnType("emp_info_arrayType");
        testOrProject(pModel, "empArray", EMP_ARRAY_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(EMP_ARRAY_OR_PROJECT);
        DatabaseQuery vrq = ds.getQuery("buildEmpArray");
        Vector args = new NonSynchronizedVector();
        args.add(Integer.valueOf(3));
        Object o = ds.executeQuery(vrq, args);
        assertTrue("return value not correct type", o instanceof BaseEntity);
        BaseEntity returnValue = (BaseEntity)o;
        Object o2 = returnValue.get(0);
        assertTrue("return value array not correct type", o2 instanceof ArrayList);
        ArrayList empInfo = (ArrayList)o2;
        assertTrue("return value array wrong size", empInfo.size() == 3);
        BaseEntity emp1 = (BaseEntity)empInfo.get(0);
        assertTrue("return value array first element id wrong value",
            emp1.get(0).equals(BigDecimal.valueOf(1)));
        assertTrue("return value array first element name wrong value",
            emp1.get(1).equals("entry 1"));
        BaseEntity emp2 = (BaseEntity)empInfo.get(1);
        assertTrue("return value array second element id wrong value",
            emp2.get(0).equals(BigDecimal.valueOf(2)));
        assertTrue("return value array second element name wrong value",
            emp2.get(1).equals("entry 2"));
        BaseEntity emp3 = (BaseEntity)empInfo.get(2);
        assertTrue("return value array third element id wrong value",
            emp3.get(0).equals(BigDecimal.valueOf(3)));
        assertTrue("return value array second element name wrong value",
            emp3.get(1).equals("entry 3"));
    }
    @SuppressWarnings("unchecked")
    @Test
    public void buildEmpArray_OxPart() throws SQLException, PublisherException,
        InstantiationException, IllegalAccessException {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("buildEmpArray");
        pModel.setCatalogPattern("another_advanced_demo");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("buildEmpArray");
        pModel.setReturnType("emp_info_arrayType");
        testOxProject(pModel, "empArray", "urn:empArray", EMPARRAY_OX_PROJECT, EMPARRAY_SCHEMA);
    }
    static final String EMPARRAY_OX_PROJECT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
           "<name>empArray</name>" +
           "<class-mapping-descriptors>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>another_advanced_demo.emp_info</class>" +
                 "<alias>emp_info</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>id</attribute-name>" +
                       "<field name=\"id/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.math.BigDecimal</attribute-classification>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>name</attribute-name>" +
                       "<field name=\"name/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.lang.String</attribute-classification>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:empArray</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/emp_infoType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>another_advanced_demo.emp_info_array_CollectionWrapper</class>" +
                 "<alias>emp_info_array</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-collection-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<reference-class>another_advanced_demo.emp_info</reference-class>" +
                       "<field name=\"item\" xsi:type=\"node\"/>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<default-root-element>emp_info_arrayType</default-root-element>" +
                 "<default-root-element-field name=\"emp_info_arrayType\" xsi:type=\"node\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:empArray</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/emp_info_arrayType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
           "</class-mapping-descriptors>" +
        "</object-persistence>";
    static final String EMPARRAY_SCHEMA = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xsd:schema targetNamespace=\"urn:empArray\" xmlns=\"urn:empArray\" elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
           "<xsd:complexType name=\"emp_infoType\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"id\" type=\"xsd:decimal\" minOccurs=\"0\"/>" +
                 "<xsd:element name=\"name\" type=\"xsd:string\" minOccurs=\"0\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"emp_info_arrayType\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"item\" type=\"emp_infoType\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:element name=\"emp_info_arrayType\" type=\"emp_info_arrayType\"/>" +
       "</xsd:schema>";

    public static final String SF_TBL1_OR_PROJECT = 
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>sfTbl1</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>toplevel.somepackage_tbl1_CollectionWrapper</class>" +
             "<alias>somepackage_tbl1</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"array-mapping\">" +
                   "<attribute-name>items</attribute-name>" +
                   "<field name=\"items\" xsi:type=\"object-relational-field\">" +
                      "<nested-type-field name=\"\" sql-typecode=\"12\" column-definition=\"VARCHAR2\" xsi:type=\"column\"/>" +
                   "</field>" +                       
                   "<container xsi:type=\"list-container-policy\">" +
                      "<collection-type>java.util.ArrayList</collection-type>" +
                   "</container>" +
                   "<structure>SOMEPACKAGE_TBL1</structure>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
       "<queries>" +
          "<query name=\"sfTbl1\" xsi:type=\"value-read-query\">" +
             "<arguments>" +
                "<argument name=\"NUM\">" +
                   "<type>java.lang.Object</type>" +
                "</argument>" +
             "</arguments>" +
             "<maintain-cache>false</maintain-cache>" +
             "<bind-all-parameters>true</bind-all-parameters>" +
             "<call xsi:type=\"stored-function-call\">" +
                "<procedure-name>SF_TBL1</procedure-name>" +
                "<cursor-output-procedure>false</cursor-output-procedure>" +
                "<arguments>" +
                   "<argument xsi:type=\"procedure-argument\">" +
                      "<procedure-argument-name>NUM</procedure-argument-name>" +
                      "<argument-name>NUM</argument-name>" +
                   "</argument>" +
                "</arguments>" +
                "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                   "<procedure-argument-type>toplevel.somepackage_tbl1_CollectionWrapper</procedure-argument-type>" +
                   "<procedure-argument-sqltype>2003</procedure-argument-sqltype>" +
                   "<procedure-argument-sqltype-name>SOMEPACKAGE_TBL1</procedure-argument-sqltype-name>" +
                "</stored-function-result>" +
             "</call>" +
          "</query>" +
       "</queries>" +
    "</object-persistence>";
    @SuppressWarnings("unchecked")
    @Test
    public void sfTbl1_OrPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("sfTbl1");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("SF_TBL1");
        pModel.setReturnType("somepackage_tbl1Type");
        testOrProject(pModel, "sfTbl1", SF_TBL1_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(SF_TBL1_OR_PROJECT);
        DatabaseQuery vrq = ds.getQuery("sfTbl1");
        Vector args = new NonSynchronizedVector();
        args.add(Integer.valueOf(3));
        Object o = ds.executeQuery(vrq, args);
        assertTrue("return value not correct type", o instanceof BaseEntity);
        BaseEntity returnValue = (BaseEntity)o;
        ArrayList<String> strings = (ArrayList<String>)returnValue.get(0);
        assertTrue("wrong number of returned strings", 3 == strings.size());
        for (int i = 0, len = strings.size(); i < len; i++) {
            assertTrue("wrong array element value", ("entry " + (i + 1)).equals(strings.get(i)));
        }
    }
    @SuppressWarnings("unchecked")
    @Test
    public void sfTbl1_OxPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("sfTbl1");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("SF_TBL1");
        pModel.setReturnType("somepackage_tbl1Type");
        testOxProject(pModel, "sfTbl1", "urn:tbl1", SF_TBL1_OX_PROJECT, SF_TBL1_SCHEMA);
    }

    public static final String SF_TBL1_OX_PROJECT = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
           "<name>sfTbl1</name>" +
           "<class-mapping-descriptors>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl1_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl1</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.lang.String</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<default-root-element>somepackage_tbl1Type</default-root-element>" +
                 "<default-root-element-field name=\"somepackage_tbl1Type\" xsi:type=\"node\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:tbl1</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_tbl1Type</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
           "</class-mapping-descriptors>" +
        "</object-persistence>";
    public static final String SF_TBL1_SCHEMA = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
        "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"urn:tbl1\" elementFormDefault=\"qualified\" targetNamespace=\"urn:tbl1\">" +
            "<xsd:complexType name=\"somepackage_tbl1Type\">" +
                "<xsd:sequence>" +
                    "<xsd:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"item\" type=\"xsd:string\"/>" +
                "</xsd:sequence>" +
            "</xsd:complexType>" +
            "<xsd:element name=\"somepackage_tbl1Type\" type=\"somepackage_tbl1Type\"/>" +
        "</xsd:schema>";

    public static final String TBL5_OR_PROJECT = 
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>tbl5</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>toplevel.somepackage_tbl5_CollectionWrapper</class>" +
             "<alias>somepackage_tbl5</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"array-mapping\">" +
                   "<attribute-name>items</attribute-name>" +
                   "<field name=\"items\" xsi:type=\"object-relational-field\">" +
                      "<nested-type-field name=\"\" sql-typecode=\"91\" column-definition=\"DATE\" xsi:type=\"column\"/>" +
                   "</field>" +
                   "<container xsi:type=\"list-container-policy\">" +
                      "<collection-type>java.util.ArrayList</collection-type>" +
                   "</container>" +
                   "<structure>SOMEPACKAGE_TBL5</structure>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
       "<queries>" +
          "<query name=\"tbl5\" xsi:type=\"value-read-query\">" +
             "<arguments>" +
                "<argument name=\"NUM\">" +
                   "<type>java.lang.Object</type>" +
                "</argument>" +
             "</arguments>" +
             "<maintain-cache>false</maintain-cache>" +
             "<bind-all-parameters>true</bind-all-parameters>" +
             "<call xsi:type=\"stored-function-call\">" +
                "<procedure-name>BUILDTBL5</procedure-name>" +
                "<cursor-output-procedure>false</cursor-output-procedure>" +
                "<arguments>" +
                   "<argument xsi:type=\"procedure-argument\">" +
                      "<procedure-argument-name>NUM</procedure-argument-name>" +
                      "<argument-name>NUM</argument-name>" +
                   "</argument>" +
                "</arguments>" +
                "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                   "<procedure-argument-type>toplevel.somepackage_tbl5_CollectionWrapper</procedure-argument-type>" +
                   "<procedure-argument-sqltype>2003</procedure-argument-sqltype>" +
                   "<procedure-argument-sqltype-name>SOMEPACKAGE_TBL5</procedure-argument-sqltype-name>" +
                "</stored-function-result>" +
             "</call>" +
          "</query>" +
       "</queries>" +
    "</object-persistence>";
    @SuppressWarnings("unchecked")
    @Test
    public void tbl5_OrPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("tbl5");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("BuildTbl5");
        pModel.setReturnType("somepackage_tbl5Type");
        testOrProject(pModel, "tbl5", TBL5_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(TBL5_OR_PROJECT);
        DatabaseQuery vrq = ds.getQuery("tbl5");
        Vector args = new NonSynchronizedVector();
        args.add(Integer.valueOf(3));
        Object o = ds.executeQuery(vrq, args);
        assertTrue("return value not correct type", o instanceof BaseEntity);
        BaseEntity returnValue = (BaseEntity)o;
        ArrayList<java.sql.Date> dates = (ArrayList<java.sql.Date>)returnValue.get(0);
        assertTrue("wrong number of returned dates", 3 == dates.size());
    }
    @SuppressWarnings("unchecked")
    @Test
    public void tbl5_OxPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("tbl5");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("BuildTbl5");
        pModel.setReturnType("somepackage_tbl5Type");
        testOxProject(pModel, "BuildTbl5", "urn:tbl5", TBL5_OX_PROJECT, TBL5_SCHEMA);
    }
    public static final String TBL5_OX_PROJECT = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
           "<name>BuildTbl5</name>" +
           "<class-mapping-descriptors>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl5_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl5</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}date</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.sql.Date</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<default-root-element>somepackage_tbl5Type</default-root-element>" +
                 "<default-root-element-field name=\"somepackage_tbl5Type\" xsi:type=\"node\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:tbl5</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_tbl5Type</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
           "</class-mapping-descriptors>" +
        "</object-persistence>";
    public static final String TBL5_SCHEMA =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xsd:schema targetNamespace=\"urn:tbl5\" xmlns=\"urn:tbl5\" elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
           "<xsd:complexType name=\"somepackage_tbl5Type\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"item\" type=\"xsd:date\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:element name=\"somepackage_tbl5Type\" type=\"somepackage_tbl5Type\"/>" +
        "</xsd:schema>";
    
    public static final String ARECORD_OR_PROJECT = 
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
       "<name>aRecord</name>" +
       "<class-mapping-descriptors>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>toplevel.somepackage_arecord</class>" +
             "<alias>somepackage_arecord</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"array-mapping\">" +
                   "<attribute-name>t1</attribute-name>" +
                   "<field name=\"T1\" xsi:type=\"object-relational-field\">" +
                      "<nested-type-field name=\"\" sql-typecode=\"12\" column-definition=\"VARCHAR2\" xsi:type=\"column\"/>" +
                   "</field>" +
                   "<container xsi:type=\"list-container-policy\">" +
                      "<collection-type>java.util.ArrayList</collection-type>" +
                   "</container>" +
                   "<structure>SOMEPACKAGE_TBL1</structure>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"array-mapping\">" +
                   "<attribute-name>t2</attribute-name>" +
                   "<field name=\"T2\" xsi:type=\"object-relational-field\">" +
                      "<nested-type-field name=\"\" sql-typecode=\"2\" column-definition=\"NUMBER\" xsi:type=\"column\"/>" +
                   "</field>" +
                   "<container xsi:type=\"list-container-policy\">" +
                      "<collection-type>java.util.ArrayList</collection-type>" +
                   "</container>" +
                   "<structure>SOMEPACKAGE_TBL2</structure>" +
                "</attribute-mapping>" +
                "<attribute-mapping xsi:type=\"direct-mapping\">" +
                   "<attribute-name>t3</attribute-name>" +
                   "<field name=\"T3\" xsi:type=\"column\"/>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
             "<structure>SOMEPACKAGE_ARECORD</structure>" +
             "<field-order>" +
                "<field name=\"T1\" xsi:type=\"column\"/>" +
                "<field name=\"T2\" xsi:type=\"column\"/>" +
                "<field name=\"T3\" xsi:type=\"column\"/>" +
             "</field-order>" +
          "</class-mapping-descriptor>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>toplevel.somepackage_tbl1_CollectionWrapper</class>" +
             "<alias>somepackage_tbl1</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"array-mapping\">" +
                   "<attribute-name>items</attribute-name>" +
                   "<field name=\"items\" xsi:type=\"object-relational-field\">" +
                      "<nested-type-field name=\"\" sql-typecode=\"12\" column-definition=\"VARCHAR2\" xsi:type=\"column\"/>" +
                   "</field>" +
                   "<container xsi:type=\"list-container-policy\">" +
                      "<collection-type>java.util.ArrayList</collection-type>" +
                   "</container>" +
                   "<structure>SOMEPACKAGE_TBL1</structure>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
          "</class-mapping-descriptor>" +
          "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
             "<class>toplevel.somepackage_tbl2_CollectionWrapper</class>" +
             "<alias>somepackage_tbl2</alias>" +
             "<events xsi:type=\"event-policy\"/>" +
             "<querying xsi:type=\"query-policy\"/>" +
             "<attribute-mappings>" +
                "<attribute-mapping xsi:type=\"array-mapping\">" +
                   "<attribute-name>items</attribute-name>" +
                   "<field name=\"items\" xsi:type=\"object-relational-field\">" +
                      "<nested-type-field name=\"\" sql-typecode=\"2\" column-definition=\"NUMBER\" xsi:type=\"column\"/>" +
                   "</field>" +
                   "<container xsi:type=\"list-container-policy\">" +
                      "<collection-type>java.util.ArrayList</collection-type>" +
                   "</container>" +
                   "<structure>SOMEPACKAGE_TBL2</structure>" +
                "</attribute-mapping>" +
             "</attribute-mappings>" +
             "<descriptor-type>aggregate</descriptor-type>" +
             "<caching>" +
                "<cache-size>-1</cache-size>" +
             "</caching>" +
             "<remote-caching>" +
                "<cache-size>-1</cache-size>" +
             "</remote-caching>" +
             "<instantiation/>" +
             "<copying xsi:type=\"instantiation-copy-policy\"/>" +
          "</class-mapping-descriptor>" +
       "</class-mapping-descriptors>" +
       "<queries>" +
          "<query name=\"buildARecord\" xsi:type=\"value-read-query\">" +
             "<arguments>" +
                "<argument name=\"NUM\">" +
                   "<type>java.lang.Object</type>" +
                "</argument>" +
             "</arguments>" +
             "<maintain-cache>false</maintain-cache>" +
             "<bind-all-parameters>true</bind-all-parameters>" +
             "<call xsi:type=\"stored-function-call\">" +
                "<procedure-name>BUILDARECORD</procedure-name>" +
                "<cursor-output-procedure>false</cursor-output-procedure>" +
                "<arguments>" +
                   "<argument xsi:type=\"procedure-argument\">" +
                      "<procedure-argument-name>NUM</procedure-argument-name>" +
                      "<argument-name>NUM</argument-name>" +
                   "</argument>" +
                "</arguments>" +
                "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                   "<procedure-argument-type>toplevel.somepackage_arecord</procedure-argument-type>" +
                   "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                   "<procedure-argument-sqltype-name>SOMEPACKAGE_ARECORD</procedure-argument-sqltype-name>" +
                "</stored-function-result>" +
             "</call>" +
          "</query>" +
       "</queries>" +
    "</object-persistence>";
    @SuppressWarnings("unchecked")
    @Test
    public void BuildARecord_OrPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("buildARecord");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("BuildARecord");
        pModel.setReturnType("somepackage_arecordType");
        testOrProject(pModel, "aRecord", ARECORD_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(ARECORD_OR_PROJECT);
        DatabaseQuery vrq = ds.getQuery("buildARecord");
        Vector args = new NonSynchronizedVector();
        int num = 3;
        args.add(Integer.valueOf(num));
        Object o = ds.executeQuery(vrq, args);
        assertTrue("return value not correct type", o instanceof BaseEntity);
        BaseEntity returnValue = (BaseEntity)o;
        ArrayList<String> tbl1 = (ArrayList<String>)returnValue.get(0);
        assertTrue("wrong number of returned strings", num == tbl1.size());
        for (int i = 0, len = tbl1.size(); i < len; i++) {
            assertTrue("wrong array element value", ("entry " + (i + 1)).equals(tbl1.get(i)));
        }
        ArrayList<BigDecimal> tbl2 = (ArrayList<BigDecimal>)returnValue.get(1);
        assertTrue("wrong number of returned strings", num == tbl2.size());
        for (int i = 0, len = tbl2.size(); i < len; i++) {
            assertTrue("wrong array element value", BigDecimal.valueOf(i+1).equals(tbl2.get(i)));
        }
        BigDecimal t3 = (BigDecimal)returnValue.get(2);
        assertTrue("wrong array element value", BigDecimal.valueOf(num).equals(t3));
    }
    @SuppressWarnings("unchecked")
    @Test
    public void BuildARecord_OxPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("buildARecord");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("BuildARecord");
        pModel.setReturnType("somepackage_arecordType");
        testOxProject(pModel, "buildARecord", "urn:aRecord", ARECORD_OX_PROJECT, ARECORD_SCHEMA);
    }
    public static final String ARECORD_OX_PROJECT = 
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
           "<name>buildARecord</name>" +
           "<class-mapping-descriptors>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_arecord</class>" +
                 "<alias>somepackage_arecord</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>t1</attribute-name>" +
                       "<field name=\"t1/item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.lang.String</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>t2</attribute-name>" +
                       "<field name=\"t2/item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.math.BigDecimal</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>t3</attribute-name>" +
                       "<field name=\"t3/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}integer</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.math.BigInteger</attribute-classification>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<default-root-element>somepackage_arecordType</default-root-element>" +
                 "<default-root-element-field name=\"somepackage_arecordType\" xsi:type=\"node\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:aRecord</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_arecordType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl1_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl1</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.lang.String</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:aRecord</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_tbl1Type</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl2_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl2</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.math.BigDecimal</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:aRecord</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_tbl2Type</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
           "</class-mapping-descriptors>" +
        "</object-persistence>";
    public static final String ARECORD_SCHEMA =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xsd:schema targetNamespace=\"urn:aRecord\" xmlns=\"urn:aRecord\" elementFormDefault=\"qualified\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" +
           "<xsd:complexType name=\"somepackage_tbl2Type\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"item\" type=\"xsd:decimal\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"somepackage_arecordType\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"t1\">" +
                    "<xsd:complexType>" +
                       "<xsd:sequence>" +
                          "<xsd:element name=\"item\" type=\"xsd:string\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>" +
                       "</xsd:sequence>" +
                    "</xsd:complexType>" +
                 "</xsd:element>" +
                 "<xsd:element name=\"t2\">" +
                    "<xsd:complexType>" +
                       "<xsd:sequence>" +
                          "<xsd:element name=\"item\" type=\"xsd:decimal\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>" +
                       "</xsd:sequence>" +
                    "</xsd:complexType>" +
                 "</xsd:element>" +
                 "<xsd:element name=\"t3\" type=\"xsd:integer\" minOccurs=\"0\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"somepackage_tbl1Type\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"item\" type=\"xsd:string\" minOccurs=\"0\" maxOccurs=\"unbounded\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:element name=\"somepackage_arecordType\" type=\"somepackage_arecordType\"/>" +
        "</xsd:schema>";        

    public static final String CRECORD_OR_PROJECT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
           "<name>cRecord</name>" +
           "<class-mapping-descriptors>" +
              "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_crecord</class>" +
                 "<alias>somepackage_crecord</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"structure-mapping\">" +
                       "<attribute-name>c1</attribute-name>" +
                       "<reference-class>toplevel.somepackage_arecord</reference-class>" +
                       "<field name=\"C1\" xsi:type=\"object-relational-field\"/>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"array-mapping\">" +
                       "<attribute-name>c2</attribute-name>" +
                       "<field name=\"C2\" xsi:type=\"object-relational-field\">" +
                          "<nested-type-field name=\"\" sql-typecode=\"2\" column-definition=\"NUMBER\" xsi:type=\"column\"/>" +
                       "</field>" +
                       "<container xsi:type=\"list-container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                       "<structure>SOMEPACKAGE_TBL2</structure>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</caching>" +
                 "<remote-caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</remote-caching>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<structure>SOMEPACKAGE_CRECORD</structure>" +
                 "<field-order>" +
                    "<field name=\"C1\" xsi:type=\"column\"/>" +
                    "<field name=\"C2\" xsi:type=\"column\"/>" +
                 "</field-order>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_arecord</class>" +
                 "<alias>somepackage_arecord</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"array-mapping\">" +
                       "<attribute-name>t1</attribute-name>" +
                       "<field name=\"T1\" xsi:type=\"object-relational-field\">" +
                          "<nested-type-field name=\"\" sql-typecode=\"12\" column-definition=\"VARCHAR2\" xsi:type=\"column\"/>" +
                       "</field>" +
                       "<container xsi:type=\"list-container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                       "<structure>SOMEPACKAGE_TBL1</structure>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"array-mapping\">" +
                       "<attribute-name>t2</attribute-name>" +
                       "<field name=\"T2\" xsi:type=\"object-relational-field\">" +
                          "<nested-type-field name=\"\" sql-typecode=\"2\" column-definition=\"NUMBER\" xsi:type=\"column\"/>" +
                       "</field>" +
                       "<container xsi:type=\"list-container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                       "<structure>SOMEPACKAGE_TBL2</structure>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"direct-mapping\">" +
                       "<attribute-name>t3</attribute-name>" +
                       "<field name=\"T3\" xsi:type=\"column\"/>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</caching>" +
                 "<remote-caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</remote-caching>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<structure>SOMEPACKAGE_ARECORD</structure>" +
                 "<field-order>" +
                    "<field name=\"T1\" xsi:type=\"column\"/>" +
                    "<field name=\"T2\" xsi:type=\"column\"/>" +
                    "<field name=\"T3\" xsi:type=\"column\"/>" +
                 "</field-order>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl1_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl1</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"array-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"items\" xsi:type=\"object-relational-field\">" +
                          "<nested-type-field name=\"\" sql-typecode=\"12\" column-definition=\"VARCHAR2\" xsi:type=\"column\"/>" +
                       "</field>" +
                       "<container xsi:type=\"list-container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                       "<structure>SOMEPACKAGE_TBL1</structure>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</caching>" +
                 "<remote-caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</remote-caching>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"object-relational-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl2_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl2</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"array-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"items\" xsi:type=\"object-relational-field\">" +
                          "<nested-type-field name=\"\" sql-typecode=\"2\" column-definition=\"NUMBER\" xsi:type=\"column\"/>" +
                       "</field>" +
                       "<container xsi:type=\"list-container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                       "<structure>SOMEPACKAGE_TBL2</structure>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</caching>" +
                 "<remote-caching>" +
                    "<cache-size>-1</cache-size>" +
                 "</remote-caching>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
              "</class-mapping-descriptor>" +
           "</class-mapping-descriptors>" +
           "<queries>" +
              "<query name=\"buildCRecord\" xsi:type=\"value-read-query\">" +
                 "<arguments>" +
                    "<argument name=\"NUM\">" +
                       "<type>java.lang.Object</type>" +
                    "</argument>" +
                 "</arguments>" +
                 "<maintain-cache>false</maintain-cache>" +
                 "<bind-all-parameters>true</bind-all-parameters>" +
                 "<call xsi:type=\"stored-function-call\">" +
                    "<procedure-name>BUILDCRECORD</procedure-name>" +
                    "<cursor-output-procedure>false</cursor-output-procedure>" +
                    "<arguments>" +
                       "<argument xsi:type=\"procedure-argument\">" +
                          "<procedure-argument-name>NUM</procedure-argument-name>" +
                          "<argument-name>NUM</argument-name>" +
                       "</argument>" +
                    "</arguments>" +
                    "<stored-function-result xsi:type=\"procedure-output-argument\">" +
                       "<procedure-argument-type>toplevel.somepackage_crecord</procedure-argument-type>" +
                       "<procedure-argument-sqltype>2002</procedure-argument-sqltype>" +
                       "<procedure-argument-sqltype-name>SOMEPACKAGE_CRECORD</procedure-argument-sqltype-name>" +
                    "</stored-function-result>" +
                 "</call>" +
              "</query>" +
           "</queries>" +
        "</object-persistence>";
    @SuppressWarnings("unchecked")
    @Test
    public void BuildCRecord_OrPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("buildCRecord");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("BuildCRecord");
        pModel.setReturnType("somepackage_crecordType");
        testOrProject(pModel, "cRecord", CRECORD_OR_PROJECT);
        // test query
        DatabaseSession ds = fixUp(CRECORD_OR_PROJECT);
        DatabaseQuery vrq = ds.getQuery("buildCRecord");
        Vector args = new NonSynchronizedVector();
        int num = 3;
        args.add(Integer.valueOf(num));
        Object o = ds.executeQuery(vrq, args);
        assertTrue("return value not correct type", o instanceof BaseEntity);
    }
    
    @Test
    public void BuildCRecord_OxPart() {
        ProcedureOperationModel pModel = new ProcedureOperationModel();
        pModel.setName("buildCRecord");
        pModel.setCatalogPattern("toplevel");
        pModel.setSchemaPattern(username.toUpperCase());
        pModel.setProcedurePattern("BuildCRecord");
        pModel.setReturnType("somepackage_crecordType");
        testOxProject(pModel, "cRecord", "urn:cRecord", CRECORD_OX_PROJECT, CRECORD_SCHEMA);
    }
    public static final String CRECORD_OX_PROJECT =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<object-persistence version=\"Eclipse Persistence Services - some version (some build date)\" xmlns=\"http://www.eclipse.org/eclipselink/xsds/persistence\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:eclipselink=\"http://www.eclipse.org/eclipselink/xsds/persistence\">" +
           "<name>cRecord</name>" +
           "<class-mapping-descriptors>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_crecord</class>" +
                 "<alias>somepackage_crecord</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-object-mapping\">" +
                       "<attribute-name>c1</attribute-name>" +
                       "<reference-class>toplevel.somepackage_arecord</reference-class>" +
                       "<field name=\"c1\" xsi:type=\"node\"/>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>c2</attribute-name>" +
                       "<field name=\"c2/item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.math.BigDecimal</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<default-root-element>somepackage_crecordType</default-root-element>" +
                 "<default-root-element-field name=\"somepackage_crecordType\" xsi:type=\"node\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:cRecord</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_crecordType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_arecord</class>" +
                 "<alias>somepackage_arecord</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>t1</attribute-name>" +
                       "<field name=\"t1/item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.lang.String</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>t2</attribute-name>" +
                       "<field name=\"t2/item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.math.BigDecimal</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                    "<attribute-mapping xsi:type=\"xml-direct-mapping\">" +
                       "<attribute-name>t3</attribute-name>" +
                       "<field name=\"t3/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}integer</schema-type>" +
                       "</field>" +
                       "<attribute-classification>java.math.BigInteger</attribute-classification>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:cRecord</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_arecordType</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl1_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl1</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}string</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.lang.String</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:cRecord</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_tbl1Type</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
              "<class-mapping-descriptor xsi:type=\"xml-class-mapping-descriptor\">" +
                 "<class>toplevel.somepackage_tbl2_CollectionWrapper</class>" +
                 "<alias>somepackage_tbl2</alias>" +
                 "<events xsi:type=\"event-policy\"/>" +
                 "<querying xsi:type=\"query-policy\"/>" +
                 "<attribute-mappings>" +
                    "<attribute-mapping xsi:type=\"xml-composite-direct-collection-mapping\">" +
                       "<attribute-name>items</attribute-name>" +
                       "<field name=\"item/text()\" xsi:type=\"node\">" +
                          "<schema-type>{http://www.w3.org/2001/XMLSchema}decimal</schema-type>" +
                       "</field>" +
                       "<value-converter xsi:type=\"type-conversion-converter\">" +
                          "<object-class>java.math.BigDecimal</object-class>" +
                       "</value-converter>" +
                       "<container xsi:type=\"container-policy\">" +
                          "<collection-type>java.util.ArrayList</collection-type>" +
                       "</container>" +
                    "</attribute-mapping>" +
                 "</attribute-mappings>" +
                 "<descriptor-type>aggregate</descriptor-type>" +
                 "<instantiation/>" +
                 "<copying xsi:type=\"instantiation-copy-policy\"/>" +
                 "<namespace-resolver>" +
                    "<default-namespace-uri>urn:cRecord</default-namespace-uri>" +
                 "</namespace-resolver>" +
                 "<schema xsi:type=\"schema-url-reference\">" +
                    "<schema-context>/somepackage_tbl2Type</schema-context>" +
                    "<node-type>complex-type</node-type>" +
                 "</schema>" +
              "</class-mapping-descriptor>" +
           "</class-mapping-descriptors>" +
        "</object-persistence>";
    public static final String CRECORD_SCHEMA =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
        "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"urn:cRecord\" elementFormDefault=\"qualified\" targetNamespace=\"urn:cRecord\">" +
           "<xsd:complexType name=\"somepackage_crecordType\">" +
              "<xsd:sequence>" +
                 "<xsd:element minOccurs=\"0\" name=\"c1\" type=\"somepackage_arecordType\"/>" +
                 "<xsd:element name=\"c2\">" +
                    "<xsd:complexType>" +
                       "<xsd:sequence>" +
                          "<xsd:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"item\" type=\"xsd:decimal\"/>" +
                       "</xsd:sequence>" +
                    "</xsd:complexType>" +
                 "</xsd:element>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"somepackage_tbl2Type\">" +
              "<xsd:sequence>" +
                 "<xsd:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"item\" type=\"xsd:decimal\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"somepackage_arecordType\">" +
              "<xsd:sequence>" +
                 "<xsd:element name=\"t1\">" +
                    "<xsd:complexType>" +
                       "<xsd:sequence>" +
                          "<xsd:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"item\" type=\"xsd:string\"/>" +
                       "</xsd:sequence>" +
                    "</xsd:complexType>" +
                 "</xsd:element>" +
                 "<xsd:element name=\"t2\">" +
                    "<xsd:complexType>" +
                       "<xsd:sequence>" +
                          "<xsd:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"item\" type=\"xsd:decimal\"/>" +
                       "</xsd:sequence>" +
                    "</xsd:complexType>" +
                 "</xsd:element>" +
                 "<xsd:element minOccurs=\"0\" name=\"t3\" type=\"xsd:integer\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:complexType name=\"somepackage_tbl1Type\">" +
              "<xsd:sequence>" +
                 "<xsd:element maxOccurs=\"unbounded\" minOccurs=\"0\" name=\"item\" type=\"xsd:string\"/>" +
              "</xsd:sequence>" +
           "</xsd:complexType>" +
           "<xsd:element name=\"somepackage_crecordType\" type=\"somepackage_crecordType\"/>" +
        "</xsd:schema>";
    
    protected void testOrProject(ProcedureOperationModel pModel, String projectName, String orProject) {
        List<DbStoredProcedure> storedProcedures = 
            OracleHelper.buildStoredProcedure(conn, username, ora11Platform, pModel); 
        Map<DbStoredProcedure, DbStoredProcedureNameAndModel> dbStoredProcedure2QueryName = 
            new HashMap<DbStoredProcedure, DbStoredProcedureNameAndModel>();
        ArrayList<OperationModel> operations = new ArrayList<OperationModel>();
        operations.add(pModel);
        DBWSBuilder.buildDbStoredProcedure2QueryNameMap(dbStoredProcedure2QueryName,
            storedProcedures, operations, true);     
        AdvancedJDBCORDescriptorBuilder advJOrDescriptorBuilder = 
            new AdvancedJDBCORDescriptorBuilder();
        AdvancedJDBCQueryBuilder queryBuilder = 
            new AdvancedJDBCQueryBuilder(storedProcedures, dbStoredProcedure2QueryName);
        PublisherListenerChainAdapter listenerChainAdapter = new PublisherListenerChainAdapter();
        listenerChainAdapter.addListener(advJOrDescriptorBuilder);
        listenerChainAdapter.addListener(queryBuilder);
        PublisherWalker walker = new PublisherWalker(listenerChainAdapter);
        pModel.getJPubType().accept(walker);
        List<ObjectRelationalDataTypeDescriptor> descriptors = 
            advJOrDescriptorBuilder.getDescriptors();
        Project p = new Project();
        p.setName(projectName);
        for (ObjectRelationalDataTypeDescriptor ordt : descriptors) { 
            p.addDescriptor(ordt);
        }
        List<DatabaseQuery> queries = queryBuilder.getQueries();
        if (queries != null && queries.size() > 0) {
            p.getQueries().addAll(queries);
        }
        Document resultDoc = xmlPlatform.createDocument();
        XMLMarshaller marshaller = new XMLContext(writeObjectPersistenceProject).createMarshaller();
        marshaller.marshal(p, resultDoc);
        Document controlDoc = xmlParser.parse(new StringReader(orProject));
        assertTrue("control document not same as instance document",
                comparer.isNodeEqual(controlDoc, resultDoc));
    }
    
    @SuppressWarnings("unchecked")
    protected void testOxProject(ProcedureOperationModel pModel, String projectName,
        String nameSpace, String oxProject, String oxSchema) {
        OracleHelper.buildStoredProcedure(conn, username, ora11Platform, pModel);
        AdvancedJDBCOXDescriptorBuilder advJOxDescriptorBuilder = 
            new AdvancedJDBCOXDescriptorBuilder(nameSpace, new TypeSuffixTransformer());
        PublisherWalker walker = new PublisherWalker(advJOxDescriptorBuilder);
        pModel.getJPubType().accept(walker);
        List<XMLDescriptor> descriptors =
            ((AdvancedJDBCOXDescriptorBuilder)walker.getListener()).getDescriptors();
        Project p = new Project();
        p.setName(projectName);
        for (XMLDescriptor xDesc : descriptors) { 
            p.addDescriptor(xDesc);
        }
        Document resultDoc = xmlPlatform.createDocument();
        XMLMarshaller marshaller = new XMLContext(writeObjectPersistenceProject).createMarshaller();
        marshaller.marshal(p, resultDoc);
        Document controlDoc = xmlParser.parse(new StringReader(oxProject));
        assertTrue("control document not same as instance document",
                comparer.isNodeEqual(controlDoc, resultDoc));
        
        SchemaModelGenerator schemaGenerator = new SchemaModelGenerator();
        SchemaModelGeneratorProperties sgProperties = new SchemaModelGeneratorProperties();
        // set element form default to qualified for target namespace
        sgProperties.addProperty(nameSpace, ELEMENT_FORM_QUALIFIED_KEY, true);
        Map schemaMap = schemaGenerator.generateSchemas(descriptors, sgProperties);
        Schema s = (Schema)schemaMap.get(nameSpace);
        Document schema = xmlPlatform.createDocument();
        new XMLContext(new SchemaModelProject()).createMarshaller().marshal(s, schema);
        Document controlSchema = xmlParser.parse(new StringReader(oxSchema));
        assertTrue("control schema not same as instance schema",
            comparer.isNodeEqual(controlSchema, schema));
    }

    @SuppressWarnings("unchecked")
    public DatabaseSession fixUp(String projectString) {
        BaseEntityClassLoader becl = new BaseEntityClassLoader(this.getClass().getClassLoader());
        XMLContext xmlContext = new XMLContext(readObjectPersistenceProject, becl);
        Project project = (Project)xmlContext.createUnmarshaller().unmarshal(
            new StringReader(projectString));
        DatabaseLogin login = new DatabaseLogin();
        login.setUserName(username);
        login.setPassword(password);
        login.setConnectionString(url);
        login.setDriverClassName(DEFAULT_DATABASE_DRIVER);
        login.setDatasourcePlatform(ora11Platform);
        login.bindAllParameters();
        project.setDatasourceLogin(login);
        for (Iterator i = project.getDescriptors().values().iterator(); i.hasNext();) {
            ClassDescriptor desc = (ClassDescriptor) i.next();
            if (!BaseEntity.class.isAssignableFrom(desc.getJavaClass())) {
                continue;
            }
            int idx = 0;
            for (Iterator j = desc.getMappings().iterator(); j.hasNext();) {
                DatabaseMapping dm = (DatabaseMapping) j.next();
                String attributeName = dm.getAttributeName();
                dm.setAttributeAccessor(new BaseEntityAccessor(attributeName, idx++));
            }
            try {
                Class clz = desc.getJavaClass();
                Method setNumAttrs = clz.getMethod("setNumAttributes", Integer.class);
                setNumAttrs.invoke(clz, new Integer(idx));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // turn-off dynamic class generation
        becl.dontGenerateSubclasses();
        DatabaseSession ds = project.createDatabaseSession();
        ds.dontLogMessages();
        //ds.setLogLevel(SessionLog.FINE);
        ds.login();
        return ds;
    }
}