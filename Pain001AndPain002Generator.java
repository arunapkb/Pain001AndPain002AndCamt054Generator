import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Pain001AndPain002Generator is a utility class for generating ISO 20022 payment initiation (pain.001)
 * and payment status report (pain.002) XML files, along with their associated meta and trigger files.
 * The generated files are used for automated payment processing and testing.
 * <p>
 * Features:
 * - Generates pain.001 and pain.002 XML files with configurable transaction and payment info counts.
 * - Creates corresponding .meta and .trigger files for each XML.
 * - Supports pretty-printing of XML for readability.
 * <p>
 * Usage:
 * Run the main method to generate sample files in the specified directory.
 */
public class Pain001AndPain002Generator {
    private static final Logger logger = Logger.getLogger(Pain001AndPain002Generator.class.getName());
    private static final String AGREEMENT_ID = "10000025"; // Agreement ID used in the generated files, should be unique for each run.
    private static final int NUMBER_OF_PAIN001_AND_PAIN002_FILES = 200; // total number of pain.001 and pain.002 files to be generated.
    private static final int NUMBER_OF_TRANSACTION_REQUIRED = 200; // number of transactions per payment info block in pain.001
    private static final int NUMBER_OF_PAYMENT_INFO_REQUIRED = 100; // number of payment info blocks in pain.001
    private static final String FILEPATH = "C:\\PMR files\\Auto Generator\\"; // Change this to your desired output directory
    private static final String PAIN001DIR = FILEPATH + "pain001\\";
    private static final String PAIN002DIR = FILEPATH + "pain002\\";
    private static final String SYSTEM = "CDS"; // System identifier used in the generated files, should match your system's identifier.
    private static final String BANKID = "3240"; // Bank ID used in the generated files, should be a valid bank identifier.
    private static final String MARKET_TYPE = "PM"; // Market type used in the generated files, should match your market type.
    private static final String[] DEBTOR = {"42010256938"}; //give valid debtor accounts
    private static final String[] CREDITOR = {"96500461516"}; //give valid creditor accounts
    private static final int INSTRUCTED_AMOUNT = 106; // Amount in NOK for each transaction, should be a valid amount for your use case.
    private static final String BIC = "HAUGNO21XXX"; // BIC code used in the generated files, should be a valid BIC for your bank.
    private static final String BANK_NAME = "Haugesund Sparebank"; // Bank name used in the generated files, should match your bank's name.


    /**
     * Entry point for generating pain.001 and pain.002 files.
     * Generates two sets of files with unique message IDs.
     */
    public static void main(String[] aa) {
        logger.info("Starting Pain001AndPain002Generator main process.");
        Pain001AndPain002Generator pain001AndPain002Generator = new Pain001AndPain002Generator();
        pain001AndPain002Generator.cleanDirectory(PAIN001DIR);
        pain001AndPain002Generator.cleanDirectory(PAIN002DIR);
        for (int i = 1; i <= NUMBER_OF_PAIN001_AND_PAIN002_FILES; i++) { // total number of files needed.
            var uniqueMessageId = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+i;
            try {
                pain001AndPain002Generator.callGeneratePain001And002(pain001AndPain002Generator, uniqueMessageId,
                        SYSTEM + "_" + BANKID + "_" + uniqueMessageId,
                        NUMBER_OF_TRANSACTION_REQUIRED,
                        NUMBER_OF_PAYMENT_INFO_REQUIRED);
                logger.info("Generated files for messageId: " + uniqueMessageId);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error generating files for messageId: " + uniqueMessageId, e);
                throw new RuntimeException(e);
            }
        }
        logger.info("Pain001AndPain002Generator main process completed.");
    }

    /**
     * Orchestrates the generation of pain.001, meta, trigger, and pain.002 files.
     *
     * @param pain001AndPain002Generator Instance of the generator
     * @param messageId                  Unique message ID for the files
     * @param fileName                   Base file name
     * @param numberOfTransRequired      Number of transactions per payment info
     * @param numberOfPIRequired         Number of payment info blocks
     * @throws IOException if file operations fail
     */
    public void callGeneratePain001And002(Pain001AndPain002Generator pain001AndPain002Generator, String messageId, String fileName, int numberOfTransRequired, int numberOfPIRequired) throws IOException {
        logger.info("Cleaning output directories before generating pain.001 and pain.002 files for messageId: " + messageId);
        pain001AndPain002Generator.generatePain001AndMeta(messageId,
                PAIN001DIR,
                fileName,
                numberOfTransRequired,
                numberOfPIRequired);
        pain001AndPain002Generator.generatePain002(messageId,
                PAIN002DIR,
                "Pain002_" + fileName,
                numberOfTransRequired,
                numberOfPIRequired);
        logger.info("Completed generation for messageId: " + messageId);
    }

    /**
     * Generates pain.001 XML, meta, and trigger files.
     *
     * @param messageId             Unique message ID
     * @param filepath              Output directory
     * @param fileName              Base file name
     * @param numberOfTransRequired Number of transactions per payment info
     * @param numberOfPIRequired    Number of payment info blocks
     * @throws IOException if file operations fail
     */
    public void generatePain001AndMeta(String messageId, String filepath, String fileName, int numberOfTransRequired, int numberOfPIRequired) throws IOException {
        logger.info("Generating pain.001 meta and trigger files for: " + fileName);
        writeMetaFiles(filepath, fileName);
        var pain001Doc = buildPain001Document(messageId, numberOfTransRequired, numberOfPIRequired);
        writeXmlFile(filepath, fileName, pain001Doc);
        createPain001XMLTriggerFile(filepath, fileName);
        logger.info("pain.001 files generated for: " + fileName);
    }

    /**
     * Writes .meta and .meta.trigger files for a given file.
     *
     * @param filepath Output directory
     * @param fileName Base file name
     * @throws IOException if file operations fail
     */
    private void writeMetaFiles(String filepath, String fileName) throws IOException {
        logger.fine("Writing meta files for: " + fileName);
        try (var metaWriter = new BufferedWriter(new FileWriter(filepath + fileName + ".meta", false), 8192 * 4)) {
            metaWriter.write("{ \"agreementId\": " + AGREEMENT_ID + ", \"parentAgreementId\": \"32323123\", \"marketType\": \"" + MARKET_TYPE + "\", \"bankId\": \"" + BANKID + "\", \"sourceSystem\": \"" + SYSTEM + "\" }");
        }
        var ignored = new BufferedWriter(new FileWriter(filepath + fileName + ".meta.trigger", false), 8192 * 4);
        logger.fine("Meta files written for: " + fileName);
    }

    /**
     * Builds the pain.001 XML document as a string.
     *
     * @param uniqueId              Unique message ID
     * @param numberOfTransRequired Number of transactions per payment info
     * @param numberOfPIRequired    Number of payment info blocks
     * @return Pretty-printed pain.001 XML string
     */
    private String buildPain001Document(String uniqueId, int numberOfTransRequired, int numberOfPIRequired) {
        logger.fine("Building pain.001 document for uniqueId: " + uniqueId);
        var cdtTrfTxInf = "<CdtTrfTxInf><PmtId><EndToEndId>MUNIQUEMSGID-PPAYINFOIDNUMBER-TTRANSACTIONIDNUMBER</EndToEndId></PmtId>" +
                "<PmtTpInf><InstrPrty>NORM</InstrPrty><CtgyPurp><Cd>DIVI</Cd></CtgyPurp></PmtTpInf><Amt><InstdAmt Ccy=\"NOK\">" + INSTRUCTED_AMOUNT + "</InstdAmt>" +
                "</Amt><CdtrAgt><FinInstnId><Othr><Id>4201</Id></Othr></FinInstnId></CdtrAgt><Cdtr><Nm>Ramesh A</Nm><CtryOfRes>NO</CtryOfRes>" +
                "</Cdtr><CdtrAcct><Id><Othr><Id>VVVVVV</Id><SchmeNm><Cd>BBAN</Cd></SchmeNm></Othr></Id><Ccy>NOK</Ccy></CdtrAcct><Purp>" +
                "<Cd>DDIV</Cd></Purp></CdtTrfTxInf>";
        var pmtInf = "<PmtInf><PmtInfId>MUNIQUEMSGID-PPAYINFOIDNUMBER</PmtInfId><PmtMtd>TRF</PmtMtd><ReqdExctnDt><Dt>2025-04-18</Dt>" +
                "</ReqdExctnDt><Dbtr><Nm>Glass stopper; co..</Nm><CtryOfRes>NO</CtryOfRes></Dbtr><DbtrAcct><Id><Othr><Id>WWWWWW</Id>" +
                "<SchmeNm><Cd>BBAN</Cd></SchmeNm></Othr></Id><Ccy>NOK</Ccy></DbtrAcct><DbtrAgt><FinInstnId><Othr><Id>4201</Id></Othr>" +
                "</FinInstnId></DbtrAgt>XXXXX</PmtInf>";
        var pain001Document = "<?xml version='1.0' encoding='UTF-8'?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.09\">" +
                "<CstmrCdtTrfInitn><GrpHdr><MsgId>MUNIQUEMSGID</MsgId><CreDtTm>2025-03-24T12:44:08.8802478</CreDtTm>" +
                "<NbOfTxs>TOTALNUMBEROFTXNS</NbOfTxs><CtrlSum>TOTALCONTROLSUM</CtrlSum><InitgPty><Id><OrgId><AnyBIC>SPTRNO22XXX</AnyBIC>" +
                "<Othr><Id>4201</Id></Othr></OrgId></Id></InitgPty></GrpHdr>YYYYY</CstmrCdtTrfInitn></Document>";
        var pmtInfoBuilder = new StringBuilder();
        int txnCount = 1;
        for (int pmtInfoNum = 1; pmtInfoNum <= numberOfPIRequired; pmtInfoNum++) {
            var txnBuilder = new StringBuilder();
            var debtorIndex = (pmtInfoNum - 1) % DEBTOR.length;
            var creditorIndex = (pmtInfoNum - 1) % CREDITOR.length;
            for (int txnNum = 1; txnNum <= numberOfTransRequired; txnNum++) {
                txnBuilder.append(cdtTrfTxInf.replace("VVVVVV", CREDITOR[creditorIndex]).replace("TRANSACTIONIDNUMBER", String.valueOf(txnNum)));
                txnCount++;
            }
            pmtInfoBuilder.append(pmtInf.replace("XXXXX", txnBuilder).replace("WWWWWW", DEBTOR[debtorIndex])
                    .replace("PAYINFOIDNUMBER", String.valueOf(pmtInfoNum)));
        }
        var result = prettyPrintXml(pain001Document
                .replace("TOTALNUMBEROFTXNS", String.valueOf(txnCount - 1))
                .replace("TOTALCONTROLSUM", String.valueOf((txnCount - 1) * INSTRUCTED_AMOUNT))
                .replace("YYYYY", pmtInfoBuilder)
                .replace("UNIQUEMSGID", String.valueOf(uniqueId)),false);
        logger.fine("pain.001 document built for uniqueId: " + uniqueId);
        return result;
    }

    /**
     * Writes the XML content to a file.
     *
     * @param filepath   Output directory
     * @param fileName   Base file name
     * @param xmlContent XML content to write
     * @throws IOException if file operations fail
     */
    private void writeXmlFile(String filepath, String fileName, String xmlContent) throws IOException {
        logger.fine("Writing XML file: " + filepath + fileName + ".xml");
        try (var writer = new BufferedWriter(new FileWriter(filepath + fileName + ".xml", false), 8192 * 4)) {
            writer.write(xmlContent);
        }
        logger.fine("XML file written: " + filepath + fileName + ".xml");
    }

    /**
     * Creates a .xml.trigger file for the pain.001 XML.
     *
     * @param filepath Output directory
     * @param fileName Base file name
     * @throws IOException if file operations fail
     */
    private void createPain001XMLTriggerFile(String filepath, String fileName) throws IOException {
        logger.fine("Creating XML trigger file: " + filepath + fileName + ".xml.trigger");
        var ignored = new BufferedWriter(new FileWriter(filepath + fileName + ".xml.trigger", false), 1);
        logger.fine("XML trigger file created: " + filepath + fileName + ".xml.trigger");
    }

    /**
     * Generates pain.002 XML and trigger files.
     *
     * @param messageId             Message ID for the report
     * @param filePath              Output directory
     * @param fileName              Base file name
     * @param numberOfTransRequired Number of transactions per payment info
     * @param numberOfPIRequired    Number of payment info blocks
     * @throws IOException if file operations fail
     */
    public void generatePain002(String messageId, String filePath, String fileName, long numberOfTransRequired, long numberOfPIRequired) throws IOException {
        logger.info("Generating pain.002 files for: " + fileName);
        // Prepare transaction and payment info and document xml
        var pain002XML = getPain002XML(messageId, numberOfTransRequired, numberOfPIRequired);

        // Write pain002 XML file
        try (var pain002XmlFile = new BufferedWriter(new FileWriter(filePath + fileName + ".xml", false), 8192 * 4)) {
            pain002XmlFile.write(pain002XML);
            pain002XmlFile.newLine();
        }
        // Create a trigger file
        var ignored = new BufferedWriter(new FileWriter(filePath + fileName + ".xml.trigger", false), 1);
        logger.info("pain.002 files generated for: " + fileName);
    }

    /**
     * Builds the pain.002 XML document as a string.
     *
     * @param messageId             Message ID for the report
     * @param numberOfTransRequired Number of transactions per payment info
     * @param numberOfPIRequired    Number of payment info blocks
     * @return Pretty-printed pain.002 XML string
     */
    private String getPain002XML(String messageId, long numberOfTransRequired, long numberOfPIRequired) {
        logger.fine("Building pain.002 document for messageId: " + messageId);
        var orgnlPmtInfAndStsTag = getOrgnlPmtInfAndStsTag(numberOfTransRequired, numberOfPIRequired);
        var document = "<?xml version='1.0' encoding='UTF-8'?><Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.002.001.10\">" +
                "<CstmrPmtStsRpt><GrpHdr><MsgId>" + AGREEMENT_ID + ".UNIQUEMSGID</MsgId><CreDtTm>2024-01-31T20:18:13.990+01:00</CreDtTm>" +
                "<InitgPty><Nm>" + BANK_NAME + "</Nm><Id><OrgId><AnyBIC>" + BIC + "</AnyBIC><Othr><Id>" + AGREEMENT_ID + "</Id><SchmeNm>" +
                "<Cd>BANK</Cd></SchmeNm></Othr></OrgId></Id></InitgPty><DbtrAgt><FinInstnId><ClrSysMmbId><ClrSysId>" +
                "<Prtry>NOBSK</Prtry></ClrSysId><MmbId>" + BANKID + "</MmbId></ClrSysMmbId></FinInstnId></DbtrAgt></GrpHdr>" +
                "<OrgnlGrpInfAndSts><OrgnlMsgId>MUNIQUEMSGID</OrgnlMsgId><OrgnlMsgNmId>pain.001.001.09</OrgnlMsgNmId>" +
                "</OrgnlGrpInfAndSts>YYYYY</CstmrPmtStsRpt></Document>";
        String result = prettyPrintXml(document.replace("YYYYY", orgnlPmtInfAndStsTag).replace("UNIQUEMSGID", messageId),false);
        logger.fine("pain.002 document built for messageId: " + messageId);
        return result;
    }

    /**
     * Builds the OrgnlPmtInfAndSts XML tags for pain.002.
     *
     * @param numberOfTransRequired Number of transactions per payment info
     * @param numberOfPIRequired    Number of payment info blocks
     * @return StringBuilder containing the XML tags
     */
    private StringBuilder getOrgnlPmtInfAndStsTag(long numberOfTransRequired, long numberOfPIRequired) {
        logger.fine("Building OrgnlPmtInfAndSts tags for pain.002");
        var orgnlPmtInfAndStsTag = new StringBuilder();
        for (long j = 1; j <= numberOfPIRequired; j++) {
            var txInfAndStsTag = new StringBuilder();
            for (long i = 1; i <= numberOfTransRequired; i++) {
                var txInfAndSts = "<TxInfAndSts><OrgnlEndToEndId>MUNIQUEMSGID-PPAYINFOIDNUMBER-TTRANSACTIONIDNUMBER</OrgnlEndToEndId>" +
                        "<TxSts>ACCP</TxSts><StsRsnInf><AddtlInf>7003013625</AddtlInf></StsRsnInf></TxInfAndSts>";
                txInfAndStsTag.append(txInfAndSts.replace("TRANSACTIONIDNUMBER", String.valueOf(i)));
            }
            var orgnlPmtInfAndSts = "<OrgnlPmtInfAndSts><OrgnlPmtInfId>MUNIQUEMSGID-PPAYINFOIDNUMBER</OrgnlPmtInfId>XXXXX</OrgnlPmtInfAndSts>";
            orgnlPmtInfAndStsTag.append(orgnlPmtInfAndSts.replace("XXXXX", txInfAndStsTag).replace("PAYINFOIDNUMBER", String.valueOf(j)));
        }
        logger.fine("OrgnlPmtInfAndSts tags built for pain.002");
        return orgnlPmtInfAndStsTag;
    }

    /**
     * Pretty-prints the given XML string.
     *
     * @param xml XML string to format
     * @return Pretty-printed XML string, or original if formatting fails
     */
    private String prettyPrintXml(String xml, boolean needed) {
        if(!needed){
            return xml;
        }
        try {
            var dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            var db = dbf.newDocumentBuilder();
            var doc = db.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            var writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (Exception e) {
            logger.warning("Failed to pretty print XML: " + e.getMessage());
            // fallback to original if pretty print fails
            return xml;
        }
    }

    // Utility method to clean a directory before generating new files
    private void cleanDirectory(String dirPath) {
        var dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                logger.info("Directory created: " + dirPath);
            } else {
                logger.warning("Failed to create directory: " + dirPath);
                return;
            }
        }
        if (dir.isDirectory()) {
            var files = dir.listFiles();
            if (files != null) {
                for (var file : files) {
                    if (!file.delete()) {
                        logger.warning("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }
}