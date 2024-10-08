package com.wadhams.financials.db.preload.app

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern

import com.wadhams.financials.db.preload.dto.SuncorpDTO
import com.wadhams.financials.db.preload.type.Status

class ConvertCSV2XMLApp {
		Pattern linePattern = ~/"(.*?)","(.*?)","(.*?)","(.*?)"/
		
		Pattern visaPurchasePattern = ~/VISA PURCHASE(.*)\d\d\/\d\d.*[A|U][U|S]D/	//ends in AUD or USD
		//Pattern visaPurchasePattern = ~/VISA PURCHASE(.*)\d\d\/\d\d.*AUD/
		Pattern visaCreditPattern = ~/VISA CREDIT(.*)\d\d\/\d\d.*AUD/
		Pattern wdlPattern  = ~/EFTPOS WDL(.*)AU/
		Pattern depPattern  = ~/EFTPOS DEP(.*)A?U?/
		Pattern bpayPattern = ~/BPAY DEBIT VIA INTERNET(.*)REFERENCE NUMBER.*/
		//Pattern ddPattern  = ~/DIRECT DEBIT(.*)\d{12}/	//DIRECT DEBIT    ORIGIN GAS 052606044487
		Pattern ddPattern  = ~/DIRECT DEBIT(.*)/
		Pattern dcPattern  = ~/DIRECT CREDIT(.*)/
		Pattern foreignPattern  = ~/FOREIGN CURRENCY CONVERSION FEE/

		NumberFormat cf		//currency format
		NumberFormat nf		//number format

		DateTimeFormatter fileRenamePrefixDTF = DateTimeFormatter.ofPattern('yyyyMMdd_HHmmss')
		String fileRenamePrefix
		
	static main(args) {
		println 'ConvertCSV2XMLApp started...'
		println ''

		ConvertCSV2XMLApp app = new ConvertCSV2XMLApp()
		app.execute()
		
		println ''
		println 'ConvertCSV2XMLApp ended.'
	}
	
	def ConvertCSV2XMLApp() {
		cf = NumberFormat.getCurrencyInstance()
		
		nf = NumberFormat.getNumberInstance()
		nf.setMinimumIntegerDigits(1)
		nf.setMinimumFractionDigits(2)
		nf.setMaximumFractionDigits(2)
		nf.setGroupingUsed(false)
		
		fileRenamePrefix = LocalDateTime.now().format(fileRenamePrefixDTF)
	}
	
	def execute() {
		File financialFile
		URL resource = getClass().getClassLoader().getResource("financial.csv")
		if (resource == null) {
			throw new IllegalArgumentException("financial.csv not found!")
		}
		else {
			financialFile = new File(resource.toURI())
		}

		println "${financialFile.name}"
		List<SuncorpDTO> suncorpDTOList = buildSuncorpDTOList(financialFile)
		println ''
		println "suncorpDTOList size: ${suncorpDTOList.size()}"
		
		//filter into two lists
		List<SuncorpDTO> validDTOList = suncorpDTOList.findAll {dto -> dto.status == Status.Valid}
		println "validDTOList size: ${validDTOList.size()}"
		List<SuncorpDTO> bypassDTOList = suncorpDTOList.findAll {dto -> dto.status == Status.Bypass}
		println "bypassDTOList size: ${bypassDTOList.size()}"
		println ''
		println 'Verify that all items below really should have been bypassed:'
		bypassDTOList.each {dto ->
			println "\tDate: ${dto.transactionDate}, Amount: ${cf.format(dto.amount)}, Description: ${dto.description}}"
		}
		
		File fout = new File("out/financial.xml")
		PrintWriter pw = fout.newPrintWriter()
		pw.print '<financials>'
		
		validDTOList.each {dto ->
			pw.print '<data>'
			
			//transactionDate
			pw.print "<dt>${dto.transactionDate}</dt>"
			
			//amount
			pw.print "<amt>${dto.amount}</amt>"
			
			pw.print "<payee>${dto.payee}</payee>"
			
			//description
			pw.print "<desc>${dto.description}</desc>"
			
			pw.print '<asset></asset>'
			
			//category
			pw.print "<cat>${dto.category}</cat>"
			
			//large transaction amounts annotate RG3
			String rg3 = ''
			if (dto.amount > 100) {
				rg3 = '$$$'
			}
			pw.print "<subcat></subcat><start></start><end></end><rg1></rg1><rg2></rg2><rg3>$rg3</rg3></data>"
		}
		pw.println '</financials>'
		pw.close()

		//backup original file with datetimestamp
		File backupFile = new File("backup/${fileRenamePrefix}_financial.csv")
		backupFile.withPrintWriter {pw2 ->
			financialFile.eachLine {line ->
				pw2.println line
			}
		}
	}
	
	List<SuncorpDTO> buildSuncorpDTOList(File csvFile) {
		List<SuncorpDTO> suncorpList = []

		csvFile.eachLine {line ->
			line = line.replaceAll(/&/, '&amp;')
			Matcher lineMatcher = line =~ linePattern
			
			if (lineMatcher.size() == 1) {
				println "Processing: $line"
				suncorpList << buildSuncorpDTO(lineMatcher)
			}
			else {
				println "Skipping..: $line"
			}
		}
		println ''
		
		return suncorpList
	}
	
	SuncorpDTO buildSuncorpDTO(Matcher lineMatcher) {
		SuncorpDTO dto = new SuncorpDTO()
		
		String regexDate = lineMatcher[0][1]
		String regexDesc = lineMatcher[0][2]
		String regexAmt = lineMatcher[0][3]
		
		//Status
		dto.status = Status.Valid	//default
		
		//transactionDate
		dto.transactionDate = regexDate
		
		//amount
		Number number = cf.parse(regexAmt)
		BigDecimal bd = new BigDecimal(nf.format(number)).negate()	//change sign
		//println bd
		dto.amount = bd
		
		Matcher descMatcher = null
		String suncorpDescription = regexDesc
		String parsedDescription = ''
		if (suncorpDescription.startsWith('VISA PURCHASE')) {
			descMatcher = suncorpDescription =~ visaPurchasePattern
			parsedDescription = descMatcher[0][1].trim()
		}
		else if (suncorpDescription.startsWith('EFTPOS WDL')) {
			descMatcher = suncorpDescription =~ wdlPattern
			parsedDescription = descMatcher[0][1].trim()
		}
		else if (suncorpDescription.startsWith('EFTPOS DEP')) {
			descMatcher = suncorpDescription =~ depPattern
			parsedDescription = descMatcher[0][1].trim()
		}
		else if (suncorpDescription.startsWith('VISA CREDIT')) {
			descMatcher = suncorpDescription =~ visaCreditPattern
			parsedDescription = descMatcher[0][1].trim()
		}
		else if (suncorpDescription.startsWith('BPAY DEBIT VIA INTERNET')) {
			descMatcher = suncorpDescription =~ bpayPattern
			parsedDescription = descMatcher[0][1].trim()
		}
		else if (suncorpDescription.startsWith('DIRECT DEBIT')) {
			descMatcher = suncorpDescription =~ ddPattern
			parsedDescription = descMatcher[0][1].trim()
		}
		else if (suncorpDescription.startsWith('DIRECT CREDIT')) {
			descMatcher = suncorpDescription =~ dcPattern
			parsedDescription = descMatcher[0][1].trim()
		}
		else if (suncorpDescription.startsWith('ATM WITHDRAWAL')) {
			parsedDescription = suncorpDescription.trim()
		}
		else if (suncorpDescription.startsWith('OSKO PAYMENT')) {
			parsedDescription = suncorpDescription.trim()
		}
		else if (suncorpDescription.startsWith('INTERNET EXTERNAL TRANSFER')) {
			parsedDescription = 'External Transfer'
		}
		else if (suncorpDescription.equals('FOREIGN CURRENCY CONVERSION FEE')) {
			parsedDescription = 'Currency Conversion Fee'
		}
		else {
			parsedDescription = suncorpDescription.trim()
			dto.status = Status.Bypass
		}
		//println parsedDescription
		derivedValuesFromDescription(parsedDescription, dto)

		return dto
	}
	
	def derivedValuesFromDescription(String parsedDescription, SuncorpDTO dto) {
		if (parsedDescription.matches(~/COLES \d\d\d\d.*/)) {
			dto.payee = 'COLES'
			dto.description = 'Groceries'
			dto.category = 'FOOD'
		}
		else if (parsedDescription.matches(~/WOOLWORTHS.*/)) {
			dto.payee = 'WOOLWORTHS'
			dto.description = 'Groceries'
			dto.category = 'FOOD'
		}
		else if (parsedDescription.matches(~/KMART.*/)) {
			dto.payee = 'KMART'
			dto.description = 'Caravan wares'
			dto.category = 'CARAVAN_EQUIPMENT'
		}
		else if (parsedDescription.matches(~/BUNNINGS.*/)) {
			dto.payee = 'BUNNINGS'
			dto.description = 'Caravan wares'
			dto.category = 'CARAVAN_EQUIPMENT'
		}
		else if (parsedDescription.matches(~/DAN MURPHY.*/)) {
			dto.payee = 'DAN MURPHYS'
			dto.description = 'Beer &amp; Wine'
			dto.category = 'ALCOHOL'
		}
		else if (parsedDescription.matches(~/1ST CHOICE.*/)) {
			dto.payee = '1ST CHOICE LIQUOR'
			dto.description = 'Beer &amp; Wine'
			dto.category = 'ALCOHOL'
		}
		else if (parsedDescription.matches(~/COLES EXPRESS.*/)) {
			dto.payee = 'COLES EXPRESS'
			dto.description = 'Fill-up'
			dto.category = 'FUEL'
		}
		else if (parsedDescription.matches(~/Belong.*/)) {
			dto.payee = 'BELONG MOBILE'
			dto.description = 'Cell phone'
			dto.category = 'PHONE_PLAN_ROB'
		}
		else if (parsedDescription.matches(~/Telstra.*/)) {
			dto.payee = 'TELSTRA'
			dto.description = 'Wifi data sim | Cell phone'
			dto.category = 'DATA_PLAN | PHONE_PLAN_MOLLY'
		}
		else if (parsedDescription.matches(~/APPLE.*/)) {
			dto.payee = 'APPLE'
			dto.description = 'Cloud Storage'
			dto.category = 'CLOUD_STORAGE'
		}
		else {
			dto.payee = 'N/A'
			dto.description = parsedDescription
			dto.category = ''
		}
	}
}
