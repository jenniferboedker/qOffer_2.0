package life.qbic.business.products.create

import life.qbic.datamodel.dtos.business.services.Product

/**
 * <h1>4.3.0 Create Service Product</h1>
 * <br>
 * <p> When the service portfolio changed due to a business decision an Offer Administrator should be allowed to provide information on the new service offered and make it available to new offers upon creation.
 * </p>
 *
 * @since: 1.0.0

 *
 */
class CreateProduct implements CreateProductInput {
    @Override
    void create(Product product) {

    }

    @Override
    void createDuplicate(Product product) {

    }
}