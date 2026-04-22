package com.wadhams.financials.db.preload.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import com.wadhams.financials.db.preload.dto.WiseDTO
import com.wadhams.financials.db.preload.type.Status

class WiseCSVService {
		DateTimeFormatter fileRenamePrefixDTF = DateTimeFormatter.ofPattern('yyyyMMdd_HHmmss')
		String fileRenamePrefix
		
	def WiseCSVService() {
		fileRenamePrefix = LocalDateTime.now().format(fileRenamePrefixDTF)
	}
	
	List<WiseDTO> buildWiseDTOList(File csvFile) {
		List<WiseDTO> wiseDTOList = []

		List<String> lineList = csvFile.readLines()
		lineList[1..-1].each {line ->
			line = line.replaceAll(/&/, '&amp;').replaceAll(/"Robert William Kent Wadhams"/, 'RWKW')
			//println line
			def splitLine = line.split(",")
			int cols = splitLine.size()
			if (cols != 20) {
				println "Invalid line split. Col: $cols Line: $splitLine"
			}
			
			WiseDTO dto = new WiseDTO()
			//CSV Status
			dto.csvStatus = Status.Valid	//default
	
			String id = trimQuotes(splitLine[0])	//trimQuotes
			def sa = id.split("-")
			dto.idPrefix = sa[0]
			dto.id = sa[1]
			
			dto.status = splitLine[1]
			dto.direction = splitLine[2]
			dto.created = trimQuotes(splitLine[3])	//trimQuotes
			dto.finished = splitLine[4]
			dto.sourceFeeAmount = splitLine[5]
			dto.sourceFeeCurrency = splitLine[6]
			dto.targetFeeAmount = splitLine[7]
			dto.targetFeeCurrency = splitLine[8]
			dto.sourceName = splitLine[9]
			dto.sourceAmountAfterFees = splitLine[10]
			dto.sourceCurrency = splitLine[11]
			dto.targetName = trimQuotes(splitLine[12])	//trimQuotes
			dto.targetAmountAfterFees = splitLine[13]
			dto.targetCurrency = splitLine[14]
			dto.exchangeRate = splitLine[15]
			dto.reference = splitLine[16]
			dto.batch = splitLine[17]
			dto.createdBy = splitLine[18]
			dto.category = trimQuotes(splitLine[19])	//trimQuotes
			
			//Bypass business rules
			if (dto.targetCurrency != 'AUD') {
				dto.csvStatus = Status.Bypass
			}
			else if (dto.status == 'CANCELLED') {	//??? no test data for this case
				dto.csvStatus = Status.Bypass
			}
			else if (dto.targetAmountAfterFees == '0.00') {
				dto.csvStatus = Status.Bypass
			}
			else if (dto.idPrefix == 'TRANSFER' && dto.direction == 'IN') {
				dto.csvStatus = Status.Bypass
			}
			else if (dto.idPrefix == 'CARD_ORDER') {
				dto.csvStatus = Status.Bypass
			}
			else if (dto.idPrefix == 'BALANCE_TRANSACTION') {
				dto.csvStatus = Status.Bypass
			}

			if (dto.status == 'REFUNDED' && dto.targetAmountAfterFees != '0.00') {
				dto.targetAmountAfterFees = '-' + dto.targetAmountAfterFees		//negate amount
			}
			
			wiseDTOList << dto
		}
		
		return wiseDTOList
	}
	
	String trimQuotes(String text) {
		if (text[0] == '"' && text[-1] == '"') {	//first and last characters are quotes
			return text[1..-2]
		}
		return text
	}
	
	File getDataFile() {
		File dataFile
		URL resource = getClass().getClassLoader().getResource("wise.csv")
		if (resource == null) {
			throw new IllegalArgumentException("wise.csv not found!")
		}
		else {
			dataFile = new File(resource.toURI())
		}
		return dataFile
	}
	
	def backupDataFile(File dataFile) {
		//backup original file with datetimestamp
		String fileRenamePrefix = LocalDateTime.now().format(fileRenamePrefixDTF)
		File backupFile = new File("backup/${fileRenamePrefix}_wise.csv")
		backupFile.withPrintWriter {pw ->
			dataFile.eachLine {line ->
				pw.println line
			}
		}
	}

}
