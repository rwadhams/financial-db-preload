package com.wadhams.financials.db.preload.service

import com.wadhams.financials.db.preload.dto.SuncorpDTO
import com.wadhams.financials.db.preload.type.Status

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Matcher
import java.util.regex.Pattern

class SuncorpCSVService {
		Pattern linePattern = ~/"(.*?)","(.*?)","(.*?)","(.*?)"/
		
		Pattern visaPurchasePattern = ~/VISA PURCHASE(.*)\d\d\/\d\d.*[A|U][U|S]D/	//ends in AUD or USD
		//Pattern visaPurchasePattern = ~/VISA PURCHASE(.*)\d\d\/\d\d.*AUD/
		Pattern visaCreditPattern = ~/VISA CREDIT(.*)\d\d\/\d\d.*AUD/
		//Pattern wdlPattern  = ~/EFTPOS WDL(.*)AU/
		Pattern wdlPattern  = ~/EFTPOS WDL(.*)/
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
		
	def SuncorpCSVService() {
		cf = NumberFormat.getCurrencyInstance()
		
		nf = NumberFormat.getNumberInstance()
		nf.setMinimumIntegerDigits(1)
		nf.setMinimumFractionDigits(2)
		nf.setMaximumFractionDigits(2)
		nf.setGroupingUsed(false)
		
		fileRenamePrefix = LocalDateTime.now().format(fileRenamePrefixDTF)
	}
	
	List<SuncorpDTO> buildSuncorpDTOList(File csvFile) {
		List<SuncorpDTO> suncorpDTOList = []

		csvFile.eachLine {line ->
			line = line.replaceAll(/&/, '&amp;')
			Matcher lineMatcher = line =~ linePattern
			
			if (lineMatcher.size() == 1) {
				println "Processing: $line"
				suncorpDTOList << buildSuncorpDTO(lineMatcher)
			}
			else {
				println "Skipping..: $line"
			}
		}
		println ''
		
		return suncorpDTOList
	}
	
	SuncorpDTO buildSuncorpDTO(Matcher lineMatcher) {
		SuncorpDTO dto = new SuncorpDTO()
		
		String regexDate = lineMatcher[0][1]
		String regexDesc = lineMatcher[0][2]
		String regexAmt = lineMatcher[0][3]
		
		//CSV Status
		dto.csvStatus = Status.Valid	//default
		
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
			dto.csvStatus = Status.Bypass
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
//		else if (parsedDescription.matches(~/Belong.*/)) {
//			dto.payee = 'BELONG MOBILE'
//			dto.description = 'Cell phone'
//			dto.category = 'PHONE_PLAN_ROB'
//		}
//		else if (parsedDescription.matches(~/Telstra.*/)) {
//			dto.payee = 'TELSTRA'
//			dto.description = 'Wifi data sim | Cell phone'
//			dto.category = 'DATA_PLAN | PHONE_PLAN_MOLLY'
//		}
//		else if (parsedDescription.matches(~/APPLE.*/)) {
//			dto.payee = 'APPLE'
//			dto.description = 'Cloud Storage'
//			dto.category = 'CLOUD_STORAGE'
//		}
		else if (parsedDescription.startsWith('Apple')) {
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
	
	File getDataFile() {
		File dataFile
		URL resource = getClass().getClassLoader().getResource("suncorp.csv")
		if (resource == null) {
			throw new IllegalArgumentException("suncorp.csv not found!")
		}
		else {
			dataFile = new File(resource.toURI())
		}
		return dataFile
	}
	
	def backupDataFile(File dataFile) {
		//backup original file with datetimestamp
		String fileRenamePrefix = LocalDateTime.now().format(fileRenamePrefixDTF)
		File backupFile = new File("backup/${fileRenamePrefix}_suncorp.csv")
		backupFile.withPrintWriter {pw ->
			dataFile.eachLine {line ->
				pw.println line
			}
		}
	}

}
