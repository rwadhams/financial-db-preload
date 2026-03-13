package com.wadhams.financials.db.preload.service

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
			pw.print '<data>'
			
			//transactionDate
			LocalDateTime ldt = LocalDateTime.parse(dto.created, transactionHistoryDTF)
			pw.print "<dt>${ldt.format(financialDTF)}</dt>"
			
			//amount
			BigDecimal bd = new BigDecimal(dto.targetAmountAfterFees)
			pw.print "<amt>$bd</amt>"

			pw.print "<payee>N/A</payee>"
			
			//description
			pw.print "<desc>${dto.targetName}</desc>"
			
			pw.print "<asset></asset><cat></cat><subcat></subcat><start></start><end></end><rg1></rg1><rg2></rg2><rg3></rg3></data>"
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
}
