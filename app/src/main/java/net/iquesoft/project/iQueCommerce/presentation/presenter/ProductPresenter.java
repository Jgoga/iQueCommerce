package net.iquesoft.project.iQueCommerce.presentation.presenter;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.util.Linkify;

import net.iquesoft.project.iQueCommerce.domain.interactor.DefaultSubscriber;
import net.iquesoft.project.iQueCommerce.domain.interactor.IsProductInWishListUseCase;
import net.iquesoft.project.iQueCommerce.domain.interactor.RemoveProductFromWishListUseCase;
import net.iquesoft.project.iQueCommerce.domain.interactor.SaveProductToWishListUseCase;
import net.iquesoft.project.iQueCommerce.presentation.model.CartModel;
import net.iquesoft.project.iQueCommerce.presentation.model.SelectedProductModel;
import net.iquesoft.project.iQueCommerce.presentation.model.ShopModel;
import net.iquesoft.project.iQueCommerce.presentation.view.fragment.ProductFragment;

import javax.inject.Inject;

import io.realm.Realm;

public class ProductPresenter implements Presenter {

    private final ShopModel shopModel;
    private final Realm realm;
    private final SaveProductToWishListUseCase saveProductToWishListUseCase;
    private final RemoveProductFromWishListUseCase removeProductFromWishListUseCase;
    private final IsProductInWishListUseCase isProductInWishListUseCase;
    private final CartModel cartModel;
    private final SelectedProductModel selectedProductModel;
    private ProductFragment fragmentView;
    private Boolean favourite = false;

    @Inject
    public ProductPresenter(SaveProductToWishListUseCase saveProductToWishListUseCase,
                            RemoveProductFromWishListUseCase removeProductFromWishListUseCase,
                            IsProductInWishListUseCase isProductInWishListUseCase,
                            SelectedProductModel selectedProductModel,
                            CartModel cartModel,
                            ShopModel shopModel) {
        this.saveProductToWishListUseCase = saveProductToWishListUseCase;
        this.removeProductFromWishListUseCase = removeProductFromWishListUseCase;
        this.isProductInWishListUseCase = isProductInWishListUseCase;
        this.selectedProductModel = selectedProductModel;
        this.cartModel = cartModel;
        this.shopModel = shopModel;
        realm = Realm.getDefaultInstance();
    }

    public void setView(ProductFragment view) {
        this.fragmentView = view;
    }

    @Override
    public void resume() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void destroy() {
        this.realm.close();
    }

    public void initialize() {
        isProductInWishList();
        this.setToolbarTitle(this.selectedProductModel.getSelectedProductModel().getTitle());
        this.fragmentView.loadContent(this.selectedProductModel.getSelectedProductModel(), this.shopModel.getMoneyFormat());
    }

    public int getCartItemCount() {
        return this.cartModel.getItemCount();
    }

    public void shareProduct(Context context) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getShareMessage());
        sendIntent.setType("text/uri");
        context.startActivity(sendIntent);
    }


    public void addToFavourite() {
        this.saveProductToWishListUseCase.setArguments(this.selectedProductModel.getSelectedProductModel().getProductId());
        this.saveProductToWishListUseCase.executeRealm(new DefaultSubscriber());
    }

    public void removeFromFavourite() {
        this.removeProductFromWishListUseCase.setArguments(this.selectedProductModel.getSelectedProductModel().getProductId());
        this.removeProductFromWishListUseCase.executeRealm(new DefaultSubscriber());
    }

    public boolean isFavourite() {
        return this.favourite;
    }

    private SpannableString getShareMessage() {
        String s = "I would like to share with you this wonderful " + this.selectedProductModel.getSelectedProductModel().getTitle()
                + "! \nCheck it out here " + this.shopModel.getUrl() + "/products/" + this.selectedProductModel.getSelectedProductModel().getHandle() + "";
        final SpannableString spannableString = new SpannableString(s);
        Linkify.addLinks(spannableString, Linkify.ALL);
        return spannableString;
    }
    private void setToolbarTitle(String title) {
        this.fragmentView.setToolbarTitle(title);
    }

    private void isProductInWishList() {
        this.isProductInWishListUseCase.setArguments(this.selectedProductModel.getSelectedProductModel().getProductId());
        this.isProductInWishListUseCase.executeRealm(new IsProductInWishListSubscriber());
    }


    private class IsProductInWishListSubscriber extends DefaultSubscriber<Boolean> {
        @Override
        public void onNext(Boolean aBoolean) {
            super.onNext(aBoolean);
            ProductPresenter.this.favourite = aBoolean;
            ProductPresenter.this.fragmentView.setFavouriteIcon(aBoolean);
        }
    }
}
