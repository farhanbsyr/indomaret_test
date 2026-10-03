package com.test.indomaret.service;

import com.test.indomaret.dto.response.PagedResponse;
import com.test.indomaret.dto.response.ProvinceResponse;
import com.test.indomaret.entity.Province;
import com.test.indomaret.exception.ResourceNotFoundException;
import com.test.indomaret.repository.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvinceService {

    private final ProvinceRepository provinceRepository;

    @Transactional(readOnly = true)
    public PagedResponse<ProvinceResponse> getAllProvinces(Pageable pageable) {
        Page<Province> page = provinceRepository.findAllByIsActiveTrueAndIsDeletedFalse(pageable);
        List<ProvinceResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();
        return PagedResponse.from(page, content);
    }

    @Transactional(readOnly = true)
    public ProvinceResponse getProvinceById(Long id) {
        Province province = provinceRepository.findByIdAndIsActiveTrueAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Province", "id", id));
        return mapToResponse(province);
    }

    @Transactional(readOnly = true)
    public ProvinceResponse getProvinceByCode(String code) {
        Province province = provinceRepository.findByCodeIgnoreCaseAndIsActiveTrueAndIsDeletedFalse(code)
                .orElseThrow(() -> new ResourceNotFoundException("Province", "code", code));
        return mapToResponse(province);
    }

    private ProvinceResponse mapToResponse(Province province) {
        return ProvinceResponse.builder()
                .id(province.getId())
                .name(province.getName())
                .code(province.getCode())
                .isActive(province.getIsActive())
                .createdAt(province.getCreatedAt())
                .updatedAt(province.getUpdatedAt())
                .build();
    }
}
