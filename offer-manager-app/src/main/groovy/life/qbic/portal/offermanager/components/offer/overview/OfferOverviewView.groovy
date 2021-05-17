package life.qbic.portal.offermanager.components.offer.overview

import com.vaadin.data.provider.DataProvider
import com.vaadin.data.provider.ListDataProvider
import com.vaadin.icons.VaadinIcons
import com.vaadin.server.FileDownloader
import com.vaadin.server.StreamResource
import com.vaadin.shared.data.sort.SortDirection
import com.vaadin.ui.Button
import com.vaadin.ui.FormLayout
import com.vaadin.ui.Grid
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Label
import com.vaadin.ui.ProgressBar
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.components.grid.HeaderRow
import com.vaadin.ui.renderers.DateRenderer
import com.vaadin.ui.themes.ValoTheme
import com.vaadin.ui.Grid.Column
import com.vaadin.ui.renderers.TextRenderer
import groovy.util.logging.Log4j2

import life.qbic.datamodel.dtos.business.Offer
import life.qbic.portal.offermanager.OfferFileNameFormatter
import life.qbic.portal.offermanager.components.offer.overview.projectcreation.CreateProjectView
import life.qbic.portal.offermanager.dataresources.offers.OfferOverview
import life.qbic.business.offers.Currency
import life.qbic.portal.offermanager.components.GridUtils


/**
 * A basic offer overview user interface.
 *
 * It presents basic available offer information, from which
 * the user can select and download one in PDF.
 *
 * @since 1.0.0
 */
@Log4j2
class OfferOverviewView extends FormLayout {

    final private OfferOverviewModel model

    final private OfferOverviewController offerOverviewController

    final private Grid<OfferOverview> overviewGrid

    final private Button downloadBtn

    final Button updateOfferBtn

    final private ProgressBar downloadSpinner

    private FileDownloader fileDownloader

    private CreateProjectView createProjectView

    private Button createProjectButton

    OfferOverviewView(OfferOverviewModel model,
                      OfferOverviewController offerOverviewController,
                      CreateProjectView createProjectView) {
        this.model = model
        this.offerOverviewController = offerOverviewController
        this.overviewGrid = new Grid<>()
        this.downloadBtn = new Button("Download Offer",VaadinIcons.DOWNLOAD)
        this.updateOfferBtn = new Button("Update Offer",VaadinIcons.EDIT)
        this.createProjectButton =  new Button("Create Project", VaadinIcons.PLUS_CIRCLE)
        this.downloadSpinner = new ProgressBar()
        this.createProjectView = createProjectView

        initLayout()
        setupGrid()
        setupListeners()
    }

    private void initLayout() {
        /*
        We start with the header, that contains a descriptive
        title of what the view is about.
         */
        final VerticalLayout headerRow = new VerticalLayout()
        final Label label = new Label("Available Offers")

        label.addStyleName(ValoTheme.LABEL_HUGE)
        headerRow.addComponent(label)
        headerRow.setMargin(false)
        this.addComponent(headerRow)

        /*
        Below the header, we create content row with two components.
        The left component will be the offer overview, the
        right component will be the offer download button.
         */
        final HorizontalLayout activityContainer = new HorizontalLayout()
        downloadBtn.setStyleName(ValoTheme.BUTTON_LARGE)
        downloadBtn.setEnabled(false)
        downloadBtn.setDescription("Download offer")
        updateOfferBtn.setStyleName(ValoTheme.BUTTON_LARGE)
        updateOfferBtn.setEnabled(false)
        updateOfferBtn.setDescription("Update offer")
        createProjectButton.setEnabled(false)
        createProjectButton.setStyleName(ValoTheme.BUTTON_LARGE)
        // Makes the progress bar a spinner
        downloadSpinner.setIndeterminate(true)
        downloadSpinner.setVisible(false)
        // Add a button to create a project from an offer
        activityContainer.addComponents(
                downloadBtn,
                updateOfferBtn,
                createProjectButton,
                downloadSpinner)

        activityContainer.setMargin(false)
        headerRow.addComponents(activityContainer,overviewGrid)
        headerRow.setSizeFull()

        this.setMargin(false)
        this.setSpacing(false)

        this.setWidthFull()
    }

    private DataProvider setupDataProvider() {
        def dataProvider = new ListDataProvider(model.offerOverviewList)
        overviewGrid.setDataProvider(dataProvider)
        return dataProvider
    }

    private void setupGrid() {
        Column<Offer, Date> dateColumn = overviewGrid.addColumn({ overview -> overview
                .getModificationDate() }).setCaption("Creation Date").setId("CreationDate")
        dateColumn.setRenderer(date -> date,  new DateRenderer('%1$tY-%1$tm-%1$td'))
        overviewGrid.addColumn({overview -> overview.offerId.toString()})
                .setCaption("Offer ID").setId("OfferId")
        overviewGrid.addColumn({overview -> overview.getProjectTitle()})
                .setCaption("Project Title").setId("ProjectTitle")
        overviewGrid.addColumn({overview -> overview.getCustomer()})
                .setCaption("Customer").setId("Customer")
        overviewGrid.addColumn({overview ->
            overview.getAssociatedProject().isPresent() ? overview.getAssociatedProject().get() :
                    "-"}).setCaption("Project ID").setId("ProjectID")
        // Format price by using a column renderer. This way the sorting will happen on the underlying double values, leading to expected behaviour.
        Column<Offer, Double> priceColumn = overviewGrid.addColumn({overview -> overview.getTotalPrice()}).setCaption("Total Price")
        priceColumn.setRenderer(price -> Currency.getFormatterWithSymbol().format(price), new TextRenderer())

        overviewGrid.sort(dateColumn, SortDirection.DESCENDING)
        overviewGrid.setWidthFull()

        def offerOverviewDataProvider = setupDataProvider()

        setupFilters(offerOverviewDataProvider)
    }

    private void setupFilters(ListDataProvider<OfferOverview> offerOverviewDataProvider) {
        HeaderRow customerFilterRow = overviewGrid.appendHeaderRow()

        GridUtils.setupColumnFilter(offerOverviewDataProvider,
                overviewGrid.getColumn("OfferId"),
                customerFilterRow)
        GridUtils.setupColumnFilter(offerOverviewDataProvider,
                overviewGrid.getColumn("ProjectTitle"),
                customerFilterRow)
        GridUtils.setupColumnFilter(offerOverviewDataProvider,
                overviewGrid.getColumn("Customer"),
                customerFilterRow)
        GridUtils.setupDateColumnFilter(offerOverviewDataProvider,
                overviewGrid.getColumn("CreationDate"),
                customerFilterRow)
    }

    private void setupListeners() {
        setupGridListeners()
        updateOfferBtn.addClickListener({
            model.offerEventEmitter.emit(model.getSelectedOffer())
        })
        createProjectButton.addClickListener({
            this.setVisible(false)
            createProjectView.setVisible(true)
            createProjectView.model.startedFromView = Optional.of(this)
            createProjectView.model.selectedOffer = Optional.of(model.selectedOffer)
        })
    }

    private void setupGridListeners() {
      log.info("debug setup grid listeners")
        overviewGrid.addSelectionListener(
                { selection ->
                    selection.firstSelectedItem.ifPresent({overview ->
                      log.info("set polling interval")
                        UI.getCurrent().setPollInterval(50)
                        log.info("make spinner visible")
                        downloadSpinner.setVisible(true)
                        log.info("create new thread object")
                        new LoadOfferInfoThread(UI.getCurrent(), overview).start()
                    })
                })
    }

    private void checkProjectCreationAllowed(OfferOverview overview) {
        if (overview.associatedProject.isPresent()) {
            createProjectButton.setEnabled(false)
        } else {
            createProjectButton.setEnabled(true)
        }
    }

    private void createResourceForDownload() {
        removeExistingResources()

        StreamResource offerResource =
                new StreamResource((StreamResource.StreamSource res) -> {
                    return model.getOfferAsPdf()
                }, OfferFileNameFormatter.getFileNameForOffer(model.getSelectedOffer()))
        fileDownloader = new FileDownloader(offerResource)
        fileDownloader.extend(downloadBtn)
    }

    private void removeExistingResources() {
        if (fileDownloader) {
            downloadBtn.removeExtension(fileDownloader)
        }
    }

    private class LoadOfferInfoThread {// extends Thread {

        final private OfferOverview offerOverview

        final private UI ui

        LoadOfferInfoThread(UI ui, OfferOverview offerOverview) {
          log.info("thread constructor")
            this.ui = ui
            this.offerOverview = offerOverview
        }
        
        void start() {
          run()
        }

//        @Override
        void run() {

            Optional<OfferOverview> selectedOffer = Optional.empty()
            log.info("ui access")
//            ui.access(() -> {
              log.info("make spinner visible in thread")
                downloadSpinner.setVisible(true)
                log.info("overviewGrid.setEnabled(false)")
                overviewGrid.setEnabled(false)
                log.info("get selected")
                selectedOffer = overviewGrid.getSelectionModel().getFirstSelectedItem()
                overviewGrid.setSelectionMode(Grid.SelectionMode.NONE)
                log.info("disable other buttons")
                downloadBtn.setEnabled(false)
                updateOfferBtn.setEnabled(false)
                createProjectButton.setEnabled(false)
//            })
            log.info("fetch offer")
                offerOverviewController.fetchOffer(offerOverview.offerId)
                log.info("create download resource")
                createResourceForDownload()
                log.info("ui access 2")
//                ui.access(() -> {
                  log.info("deactivate spinner")
                    downloadSpinner.setVisible(false)
                    overviewGrid.setSelectionMode(Grid.SelectionMode.SINGLE)
                    // After we have set the single mode to NONE, the listeners seem to be gone
                    // So we set them again
                    // IMPORTANT: the selection must be set before we attach the listener,
                    // otherwise the selection listener gets triggered (LOOP!)
                    log.info("select")
                    overviewGrid.select(selectedOffer.get())
                    log.info("setup listeners")
                    setupGridListeners()
                    log.info("activate buttons")
                    overviewGrid.setEnabled(true)
                    downloadBtn.setEnabled(true)
                    updateOfferBtn.setEnabled(true)
                    log.info("check creation allowed")
                    checkProjectCreationAllowed(offerOverview)
                    log.info("stop polling")
//                    ui.setPollInterval(-1)
//                })
        }
    }

}
