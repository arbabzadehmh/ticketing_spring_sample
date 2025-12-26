package ir.controller.api;

import ir.service.AddressClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/rest/addresses")
public class AddressApi {

    private final AddressClient addressClient;

    public AddressApi(AddressClient addressClient) {
        this.addressClient = addressClient;
    }

    @GetMapping
    public ResponseEntity<?> getAddresses() {
        return ResponseEntity.ok(addressClient.findAll());
    }

}
