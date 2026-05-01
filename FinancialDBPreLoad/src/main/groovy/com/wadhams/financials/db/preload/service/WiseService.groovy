package com.wadhams.financials.db.preload.service

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.wadhams.financials.db.preload.dto.DerivedValuesDTO
import com.wadhams.financials.db.preload.dto.WiseDTO
import com.wadhams.financials.db.preload.type.Status

class WiseService {
	DateTimeFormatter transactionHistoryDTF = DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm:ss')
	DateTimeFormatter financialDTF = DateTimeFormatter.ofPattern('dd/MM/yyyy')
	NumberFormat cf = NumberFormat.getCurrencyInstance()
	
	List<WiseDTO> filterDTOList(List<WiseDTO> wiseDTOList, Status status) {
		return wiseDTOList.findAll {dto -> dto.csvStatus == status}
	}
	
	def reportBypassItems(List<WiseDTO> bypassDTOList) {
		println 'Verify that all items below really should have been bypassed:'
		bypassDTOList.each {dto ->
			println "\t${dto.displayValues()}"
//			BigDecimal amt = new BigDecimal(dto.targetAmountAfterFees)
//			println "\tid: ${dto.id} Date: ${dto.created}, Amount: ${cf.format(amt)}, Description: ${dto.targetName}}"
		}
	}
	
	def writeValidItemFile(List<WiseDTO> validDTOList) {
		File fout = new File("out/financial.xml")
		PrintWriter pw = fout.newPrintWriter()
		pw.print '<financials>'
		
		validDTOList.each {dto ->
			DerivedValuesDTO derivedValuesDTO = buildDerivedValues(dto)
			
			pw.print '<data>'
			
			//transactionDate
			LocalDateTime ldt = LocalDateTime.parse(dto.created, transactionHistoryDTF)
			pw.print "<dt>${ldt.format(financialDTF)}</dt>"
			
			//amount
			BigDecimal bd = new BigDecimal(dto.targetAmountAfterFees)
			pw.print "<amt>$bd</amt>"

			pw.print "<payee>${derivedValuesDTO.payee}</payee>"
			pw.print "<desc>${derivedValuesDTO.description}</desc>"
			pw.print "<asset></asset>"
			pw.print "<cat>${derivedValuesDTO.category}</cat>"
			
			pw.print "<subcat></subcat><start></start><end></end><rg1></rg1><rg2></rg2><rg3></rg3></data>"
		}
		pw.println '</financials>'
		pw.close()
	}
	
	List<WiseDTO> combineDuplicateIds(List<WiseDTO> wiseList) {
		List<WiseDTO> combinedWiseDTOList = []

		println 'combineDuplicateIds...'
		int index = 0
		WiseDTO currentDTO = readWiseDTO(index, wiseList)
		String savedId = ''
		WiseDTO outDTO = null
		while (currentDTO != null) {
			savedId = currentDTO.id
			while (currentDTO != null && currentDTO.id == savedId) {
				if (outDTO == null) {
					outDTO = new WiseDTO(currentDTO)
				}
				else {
					//add current to out
					BigDecimal amt1 = new BigDecimal(outDTO.targetAmountAfterFees)
					BigDecimal amt2 = new BigDecimal(currentDTO.targetAmountAfterFees)
					BigDecimal combined = amt1.add(amt2)
					outDTO.targetAmountAfterFees = combined.toString()
					println "\t${outDTO.id}\t${amt1} + ${amt2} = ${combined}"
				}
				index++
				currentDTO = readWiseDTO(index, wiseList)
			}
			combinedWiseDTOList << outDTO
			outDTO = null
		}
		println ''
		
		return combinedWiseDTOList
	}
	
	WiseDTO readWiseDTO(int index, List<WiseDTO> wiseList) {
		if (index < wiseList.size()) {
			return wiseList[index]
		}
		return null
	}
	
	DerivedValuesDTO buildDerivedValues(WiseDTO wiseDTO) {
		DerivedValuesDTO dto = new DerivedValuesDTO()
		
		String description = wiseDTO.targetName
		if (wiseDTO.note) {
			description = description + " - ${wiseDTO.note}"
		}
		
		if (wiseDTO.targetName == 'Coles Supermarkets') {
			dto.payee = 'COLES'
			dto.description = 'Groceries'
			dto.category = 'FOOD'
		}
		else if (wiseDTO.targetName == 'Woolworths Supermarkets') {
			dto.payee = 'WOOLWORTHS'
			dto.description = 'Groceries'
			dto.category = 'FOOD'
		}
		else if (wiseDTO.targetName.startsWith('Dan Murphy')) {
			dto.payee = 'DAN MURPHYS'
			dto.description = 'Beer &amp; Wine'
			dto.category = 'ALCOHOL'
		}
		else if (wiseDTO.targetName.startsWith('BWS')) {
			dto.payee = 'BWS'
			dto.description = 'Beer &amp; Wine'
			dto.category = 'ALCOHOL'
		}
		else if (wiseDTO.targetName == 'Chemist Warehouse') {
			dto.payee = 'CHEMIST WAREHOUSE'
			dto.description = 'Pills'
			dto.category = 'PHARMACY'
		}
		else if (wiseDTO.targetName == 'Bunnings') {
			dto.payee = 'BUNNINGS'
			dto.description = ''
			dto.category = ''
		}
		else if (wiseDTO.targetName.trim() == 'Kmart') {
			dto.payee = 'KMART'
			dto.description = ''
			dto.category = ''
		}
		else if (wiseDTO.targetName == 'Supercheap Auto') {
			dto.payee = 'SUPER CHEAP AUTO'
			dto.description = ''
			dto.category = ''
		}
		else {
			dto.payee = 'N/A'
			dto.description = description
			dto.category = ''
		}

		return dto
	}
}
