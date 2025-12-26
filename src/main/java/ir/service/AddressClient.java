package ir.service;

import ir.dto.AddressDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(
        name = "address-service",
        url = "${address.service.url}"
)
public interface AddressClient {

    @PostMapping("/rest/addresses/by-ids")
    Map<Long, AddressDto> findByIds(@RequestBody List<Long> ids);

    @GetMapping("/rest/addresses")
    List<AddressDto> findAll();
}
