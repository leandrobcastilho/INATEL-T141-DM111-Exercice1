package br.com.leandro.gae_exemplo1.controller;

import br.com.leandro.gae_exemplo1.model.Product;
import br.com.leandro.gae_exemplo1.util.CheckRole;
import com.google.appengine.api.datastore.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import java.util.logging.Logger;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping(path = "/api/products")
public class ProductController {

    private static final Logger log = Logger.getLogger("ProductController");

    /*
    {
        log.finest("Mensagem de nível DEBUG");
        log.finer("Mensagem de	nível DEBUG");
        log.fine("Mensagem	de nível DEBUG");
        log.config("Mensagem de nível DEBUG");
        log.info("Mensagem	de nível INFO");
        log.warning("Mensagem de nível	WARNING");
        log.severe("Mensagem de nível ERROR")

        System.out.println("Mensagem	INFO");
        System.err.println("Mensagem	WARNING");
    }
     */

    @PreAuthorize("hasAnyAuthority('" + CheckRole.ROLE_USER + "','" + CheckRole.ROLE_ADMIN + "')")
    @GetMapping("/{code}")
    public ResponseEntity<Product> getProduct(@PathVariable int code) {

        System.out.println("getUsers");
        Optional<Product> opProduct = findProduct(code);
        if( opProduct.isPresent() ){
            Product product = opProduct.get();
            return new ResponseEntity<Product>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        //DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        //Query.Filter codeFilter = new Query.FilterPredicate(PROPERTY_PRODUCTS_CODE, Query.FilterOperator.EQUAL, code);
        //Query query = new Query(KIND_PRODUCTS).setFilter(codeFilter);
        //Entity productEntity = dataStore.prepare(query).asSingleEntity();
        //if (productEntity != null) {
        //    Product product = entityToProduct(productEntity);
        //    return new ResponseEntity<Product>(product, HttpStatus.OK);
        //} else {
        //    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        //}

    }

    public static Optional<Product> findProduct(int code) {

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter codeFilter = new Query.FilterPredicate(PROPERTY_PRODUCTS_CODE, Query.FilterOperator.EQUAL, code);
        Query query = new Query(KIND_PRODUCTS).setFilter(codeFilter);
        Entity productEntity = datastore.prepare(query).asSingleEntity();
        if (productEntity != null) {
            return Optional.ofNullable(entityToProduct(productEntity));
        } else {
            return Optional.empty();
        }
    }

    @PreAuthorize("hasAnyAuthority('" + CheckRole.ROLE_USER + "','" + CheckRole.ROLE_ADMIN + "')")
    @GetMapping
    public ResponseEntity<List<Product>> getProducts() {

        System.out.println("getUsers");
        List<Product> products = new ArrayList<>();
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        Query query;
        query = new Query(KIND_PRODUCTS).addSort(PROPERTY_PRODUCTS_CODE, Query.SortDirection.ASCENDING);
        List<Entity> productsEntities = dataStore.prepare(query).asList(FetchOptions.Builder.withDefaults());
        for (Entity productEntity : productsEntities) {
            Product product = entityToProduct(productEntity);
            products.add(product);
        }

        return new ResponseEntity<List<Product>>(products, HttpStatus.OK);

    }

    @PreAuthorize("hasAnyAuthority('" + CheckRole.ROLE_ADMIN + "')")
    @PostMapping
    public ResponseEntity<Product> saveProduct(@RequestBody Product product) {

        System.out.println("getUsers");
        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter codeFilter = new Query.FilterPredicate(PROPERTY_PRODUCTS_CODE, Query.FilterOperator.EQUAL, product.getCode());
        Query query = new Query(KIND_PRODUCTS).setFilter(codeFilter);
        Entity productEntityAux = dataStore.prepare(query).asSingleEntity();
        if (productEntityAux != null)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Key productKey = KeyFactory.createKey(KIND_PRODUCTS, KIND_PRODUCTS_KEY);
        Entity productEntity = new Entity(KIND_PRODUCTS, productKey);
        this.productToEntity(product, productEntity);
        dataStore.put(productEntity);
        product.setId(productEntity.getKey().getId());

        return new ResponseEntity<Product>(product, HttpStatus.CREATED);

    }

    @PreAuthorize("hasAnyAuthority('" + CheckRole.ROLE_ADMIN + "')")
    @DeleteMapping(path = "/{code}")
    public ResponseEntity<Product> deleteProduct(@PathVariable("code") int code) {

        log.fine("Tentando	apagar	produto	com	código = [" + code + "]");

        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();
        Query.Filter codeFilter = new Query.FilterPredicate(PROPERTY_PRODUCTS_CODE, Query.FilterOperator.EQUAL, code);
        Query query = new Query(KIND_PRODUCTS).setFilter(codeFilter);
        Entity productEntity = dataStore.prepare(query).asSingleEntity();
        if (productEntity != null) {
            dataStore.delete(productEntity.getKey());
            log.info("Produto com código = [" + code + "]	" + "apagado com sucesso");
            Product product = entityToProduct(productEntity);
            return new ResponseEntity<Product>(product, HttpStatus.OK);
        } else {
            log.severe("Erro ao apagar	produto	com	código = [" + code + "]. Produto não encontrado!");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @PreAuthorize("hasAnyAuthority('" + CheckRole.ROLE_ADMIN + "')")
    @PutMapping(path = "/{code}")
    public ResponseEntity<Product> updateProduct(@RequestBody Product product, @PathVariable("code") int code) {

        if (product.getId() <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

        Query.Filter codeFilter = new Query.FilterPredicate(PROPERTY_PRODUCTS_CODE, Query.FilterOperator.EQUAL, code);
        Query query = new Query(KIND_PRODUCTS).setFilter(codeFilter);
        Entity productEntity = dataStore.prepare(query).asSingleEntity();
        if (productEntity != null) {

            Query.Filter codeFilterAux = new Query.FilterPredicate(PROPERTY_PRODUCTS_CODE, Query.FilterOperator.EQUAL, product.getCode());
            Query queryAux = new Query(KIND_PRODUCTS).setFilter(codeFilterAux);
            Entity productEntityAux = dataStore.prepare(queryAux).asSingleEntity();
            if (productEntityAux != null)
                if (productEntity.getKey().getId() != productEntityAux.getKey().getId())
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            productToEntity(product, productEntity);
            dataStore.put(productEntity);
            product.setId(productEntity.getKey().getId());
            return new ResponseEntity<Product>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    private void productToEntity(Product product, Entity productEntity) {

        productEntity.setProperty(PROPERTY_PRODUCTS_PRODUCT_ID, product.getProductID());
        productEntity.setProperty(PROPERTY_PRODUCTS_NAME, product.getName());
        productEntity.setProperty(PROPERTY_PRODUCTS_CODE, product.getCode());
        productEntity.setProperty(PROPERTY_PRODUCTS_MODEL, product.getModel());
        productEntity.setProperty(PROPERTY_PRODUCTS_PRICE, product.getPrice());

    }

    private static Product entityToProduct(Entity productEntity) {

        Product product = new Product();
        product.setId(productEntity.getKey().getId());
        product.setProductID((String) productEntity.getProperty(PROPERTY_PRODUCTS_PRODUCT_ID));
        product.setName((String) productEntity.getProperty(PROPERTY_PRODUCTS_NAME));
        product.setCode(Integer.parseInt(productEntity.getProperty(PROPERTY_PRODUCTS_CODE).toString()));
        product.setModel((String) productEntity.getProperty(PROPERTY_PRODUCTS_MODEL));
        product.setPrice(Float.parseFloat(productEntity.getProperty(PROPERTY_PRODUCTS_PRICE).toString()));

        return product;

    }

    private static final String KIND_PRODUCTS = "Products";
    private static final String KIND_PRODUCTS_KEY = "productKey";

    private static final String PROPERTY_PRODUCTS_PRODUCT_ID = "ProductID";
    private static final String PROPERTY_PRODUCTS_NAME = "Name";
    private static final String PROPERTY_PRODUCTS_CODE = "Code";
    private static final String PROPERTY_PRODUCTS_MODEL = "Model";
    private static final String PROPERTY_PRODUCTS_PRICE = "Price";
}
