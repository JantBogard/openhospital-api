/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.vactype.rest;

import java.util.List;

import org.isf.shared.exceptions.OHAPIException;
import org.isf.utils.exception.OHDataIntegrityViolationException;
import org.isf.utils.exception.OHServiceException;
import org.isf.utils.exception.model.OHExceptionMessage;
import org.isf.vactype.dto.VaccineTypeDTO;
import org.isf.vactype.manager.VaccineTypeBrowserManager;
import org.isf.vactype.mapper.VaccineTypeMapper;
import org.isf.vactype.model.VaccineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController(value = "/vaccinetypes")
@Tag(name = "Vaccine Type")
@SecurityRequirement(name = "bearerAuth")
public class VaccineTypeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VaccineTypeController.class);

	@Autowired
	protected VaccineTypeBrowserManager vaccineTypeManager;

	@Autowired
	protected VaccineTypeMapper mapper;

	public VaccineTypeController(VaccineTypeBrowserManager vaccineTypeManager, VaccineTypeMapper vaccineTypeMapper) {
		this.vaccineTypeManager = vaccineTypeManager;
		this.mapper = vaccineTypeMapper;
	}

	/**
	 * Get all the vaccine types.
	 *
	 * @return
	 * @throws OHServiceException
	 */
	@GetMapping(value = "/vaccinetypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<VaccineTypeDTO>> getVaccineType() throws OHServiceException {
		LOGGER.info("Get vaccine types.");
		List<VaccineType> vaccinesTypes = vaccineTypeManager.getVaccineType();
		List<VaccineTypeDTO> listVaccines = mapper.map2DTOList(vaccinesTypes);
		if (listVaccines.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body(listVaccines);
		} else {
			return ResponseEntity.ok(listVaccines);
		}
	}

	/**
	 * Create a new vaccine type.
	 *
	 * @param newVaccineType
	 * @return an error message if there are some problem, ok otherwise
	 * @throws OHServiceException
	 */
	@PostMapping(value = "/vaccinetypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<VaccineTypeDTO> newVaccineType(@RequestBody VaccineTypeDTO newVaccineType) throws OHServiceException {
		LOGGER.info("Create vaccine type: {}", newVaccineType);
		VaccineType newVaccineTYpe;
		try {
			newVaccineTYpe = vaccineTypeManager.newVaccineType(mapper.map2Model(newVaccineType));
		} catch (OHDataIntegrityViolationException e) {
			throw new OHAPIException(new OHExceptionMessage("Vaccine Type already present."));
		}
		if (newVaccineTYpe == null) {
			throw new OHAPIException(new OHExceptionMessage("Vaccine Type not created."));
		}
		return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map2DTO(newVaccineTYpe));
	}

	/**
	 * Update a vaccine type.
	 *
	 * @param updateVaccineType
	 * @return an error message if there are some problem, ok otherwise
	 * @throws OHServiceException
	 */
	@PutMapping(value = "/vaccinetypes", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<VaccineTypeDTO> updateVaccineType(@RequestBody VaccineTypeDTO updateVaccineType) throws OHServiceException {
		LOGGER.info("Update vaccine type: {}", updateVaccineType);
		VaccineType updatedVaccineType;
		try {
			updatedVaccineType = vaccineTypeManager.updateVaccineType(mapper.map2Model(updateVaccineType));
		} catch (OHServiceException serviceException) {
			throw new OHAPIException(new OHExceptionMessage("Vaccine Type not updated."));
		}
		return ResponseEntity.ok(mapper.map2DTO(updatedVaccineType));

	}

	/**
	 * Delete a vaccine type.
	 *
	 * @param code the vaccineType to delete
	 * @return an error message if there are some problem, ok otherwise
	 * @throws OHServiceException
	 */
	@DeleteMapping(value = "/vaccinetypes/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> deleteVaccineType(@PathVariable String code) throws OHServiceException {
		LOGGER.info("Delete vaccine type code: {}", code);
		VaccineType vaccineType = vaccineTypeManager.findVaccineType(code);
		if (vaccineType != null) {
			try {
				vaccineTypeManager.deleteVaccineType(vaccineType);
			} catch (OHServiceException serviceException) {
				throw new OHAPIException(new OHExceptionMessage("Vaccine Type not deleted."));
			}
			return ResponseEntity.ok(true);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	/**
	 * Check if the code is already used by other vaccine type.
	 *
	 * @param code
	 * @return {@code true} if it is already used, {@code false} otherwise
	 * @throws OHServiceException
	 */
	@GetMapping(value = "/vaccinetypes/check/{code}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> checkVaccineTypeCode(@PathVariable String code) throws OHServiceException {
		LOGGER.info("Check vaccine type code: {}", code);
		boolean check = vaccineTypeManager.isCodePresent(code);
		return ResponseEntity.ok(check);
	}
}
