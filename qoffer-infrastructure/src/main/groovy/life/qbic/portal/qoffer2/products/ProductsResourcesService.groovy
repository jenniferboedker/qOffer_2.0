package life.qbic.portal.qoffer2.products


import life.qbic.datamodel.dtos.business.services.Product
import life.qbic.portal.qoffer2.events.EventEmitter
import life.qbic.portal.qoffer2.services.ResourcesService

/**
 * Product service that holds resources about available products
 *
 * This service holds a list of available products and exposes an
 * event emitter, that can be used to subscribe to any update
 * event of the underlying resource data.
 *
 * @since: 1.0.0
 */
class ProductsResourcesService implements ResourcesService {

    private final ProductsDbConnector dbConnector
    private final List<Product> products
    /**
     * EventEmitter for products. Fires every time the resources are reloaded
     * @see #reloadResources
     */
    final EventEmitter<List<Product>> productEventEmitter


    /**
     * Constructor expecting a customer database connector
     * @param dbConnector
     */
    ProductsResourcesService(ProductsDbConnector dbConnector) {
        this.dbConnector = dbConnector
        this.products = new LinkedList<>()
        this.productEventEmitter = new EventEmitter<>()
        reloadResources()
    }

    @Override
    void reloadResources() {
        this.products.clear()
        this.products.addAll(dbConnector.findAllAvailableProducts())
        this.productEventEmitter.emit(List.copyOf(this.products))
    }

    /**
     *
     * @return currently loaded available products
     */
    List<Product> getProducts() {
        return List.copyOf(this.products)
    }
}