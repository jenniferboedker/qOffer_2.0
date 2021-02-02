package life.qbic.portal.offermanager.dataresources.products


import life.qbic.datamodel.dtos.business.services.Product
import life.qbic.portal.offermanager.communication.EventEmitter
import life.qbic.portal.offermanager.communication.Subscription
import life.qbic.portal.offermanager.dataresources.ResourcesService

/**
 * Product service that holds resources about available products
 *
 * This service holds a list of available products and exposes an
 * event emitter, that can be used to subscribe to any update
 * event of the underlying resource data.
 *
 * @since: 1.0.0
 */
class ProductsResourcesService implements ResourcesService<Product> {

    private final ProductsDbConnector dbConnector
    private final List<Product> products
    /**
     * EventEmitter for products. Fires every time the resources are reloaded
     * @see #reloadResources
     */
    private final EventEmitter<Product> productEventEmitter


    /**
     * Constructor expecting a customer database connector
     * @param dbConnector
     */
    ProductsResourcesService(ProductsDbConnector dbConnector) {
        this.dbConnector = dbConnector
        this.products = new LinkedList<>()
        this.productEventEmitter = new EventEmitter<>()
        populateResources()
    }

    @Override
    @Deprecated(since = "1.0.0", forRemoval = true)
    void reloadResources() {
        this.products.clear()
        this.products.addAll(dbConnector.findAllAvailableProducts())
    }

    private void populateResources() {
        this.products.addAll(dbConnector.findAllAvailableProducts())
    }

    @Override
    void addToResource(Product resourceItem) {
        this.products.add(resourceItem)
        productEventEmitter.emit(resourceItem)
    }

    @Override
    void removeFromResource(Product resourceItem) {
        this.products.remove(resourceItem)
        productEventEmitter.emit(resourceItem)
    }

    @Override
    void subscribe(Subscription<Product> subscription) {
        this.productEventEmitter.register(subscription)

    }

    @Override
    void unsubscribe(Subscription<Product> subscription) {
        this.productEventEmitter.unregister(subscription)
    }


    /**
     * @inheritdoc
     *
     * @return An iterator over the list of available products
     */
    @Override
    Iterator<Product> iterator() {
        return List.copyOf(this.products).iterator()
    }

    /**
     *
     * @return currently loaded available products
     * @deprecated please use {@link #iterator()} instead
     */
    @Deprecated(since = "1.0.0", forRemoval = true)
    List<Product> getProducts() {
        return List.copyOf(this.products)
    }
}
