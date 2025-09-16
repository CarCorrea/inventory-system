package com.challenge.meli.inventory.service;

import com.challenge.meli.inventory.domain.Inventory;
import com.challenge.meli.inventory.domain.Product;
import com.challenge.meli.inventory.domain.Store;
import com.challenge.meli.inventory.repository.InventoryRepository;
import com.challenge.meli.inventory.repository.ProductRepository;
import com.challenge.meli.inventory.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataInitializationService implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationService.class);

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final InventoryRepository inventoryRepository;

    public DataInitializationService(ProductRepository productRepository, StoreRepository storeRepository, InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Initializing sample data ");

        initializeProducts();
        initializeStores();
        initializeInventory();

        logger.info("Sample data initialization completed");
    }

    private void initializeProducts() {
        List<Product> products = List.of(
                new Product("SKU001", "Smartphone Samsung Galaxy", "Latest Samsung smartphone", 899.99),
                new Product("SKU002", "Laptop MacBook Pro", "MacBook Pro 14-inch", 1999.99),
                new Product("SKU003", "Auriculares Sony", "Auriculares inalámbricos Sony", 199.99),
                new Product("SKU004", "Tablet iPad", "iPad de 10.9 pulgadas", 549.99),
                new Product("SKU005", "Smart TV 55\"", "Smart TV LED 55 pulgadas", 699.99)
        );

        productRepository.saveAll(products);
        logger.info("Initialized {} products", products.size());
    }

    private void initializeStores() {
        List<Store> stores = List.of(
                new Store("STORE001", "Tienda Centro", "Av. Providencia 123, Santiago", -33.4489, -70.6693),
                new Store("STORE002", "Tienda Las Condes", "Av. Kennedy 456, Las Condes", -33.4170, -70.5489),
                new Store("STORE003", "Tienda Ñuñoa", "Av. Grecia 789, Ñuñoa", -33.4569, -70.5956),
                new Store("STORE004", "Tienda Maipú", "Av. Pajaritos 321, Maipú", -33.5089, -70.7575),
                new Store("STORE005", "Tienda Valparaíso", "Calle Condell 654, Valparaíso", -33.0472, -71.6127)
        );

        storeRepository.saveAll(stores);
        logger.info("Initialized {} stores", stores.size());
    }

    private void initializeInventory() {
        List<Product> products = productRepository.findAll();
        List<Store> stores = storeRepository.findAll();

        for (Product product : products) {
            for (Store store : stores) {
                int stock = (int) (Math.random() * 41) + 10;
                Inventory inventory = new Inventory(product.getProductId(), store.getStoreId(), stock);
                inventoryRepository.save(inventory);
            }
        }

        logger.info("Initialized inventory for {} product-store combinations",
                products.size() * stores.size());
    }
}
