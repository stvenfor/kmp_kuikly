import { KRNativeRenderController, KRRecord, Kuikly } from '@kuikly-open/render';
import globalNativeManager from '../kuikly/MyNativeManager';
import { KuiklyViewDelegate } from '../kuikly/KuiklyViewDelegate';
import router from '@ohos.router';

@Entry
@Component
struct Index {
  private kuiklyViewDelegate = new KuiklyViewDelegate();
  private kuiklyController: KRNativeRenderController | null = null;
  private pageName: string | null = null;
  private pageData?: KRRecord;
  @State showKuikly: boolean = true;

  onBackPress(): boolean | void {
    if (this.kuiklyController) {
      return this.kuiklyController.onBackPress();
    }
  }

  aboutToAppear(): void {
    const params = router.getParams() as Record<string, Object>;
    if (params) {
      this.pageName = params.pageName as string;
      this.pageData = (params.pageData as KRRecord | null) ?? {};
    }
    if (!this.pageName) {
      const stored = AppStorage.get('kuiklyPageName') as string;
      if (stored) {
        this.pageName = stored;
      }
    }
    if (!this.pageData || Object.keys(this.pageData).length === 0) {
      // golden capture priming: aa start --ps pageData '<json>' → AppStorage
      const storedData = AppStorage.get('kuiklyPageData') as string;
      if (storedData) {
        try {
          this.pageData = JSON.parse(storedData) as KRRecord;
        } catch (e) {
          this.pageData = {};
        }
      }
    }
  }

  onPageShow(): void {
    this.kuiklyViewDelegate.pageDidAppear();
  }

  onPageHide(): void {
    this.kuiklyViewDelegate.pageDidDisappear();
  }

  build() {
    Stack() {
      if (this.showKuikly) {
        Kuikly({
          pageName: this.pageName ?? 'Splash',
          pageData: this.pageData ?? {},
          delegate: this.kuiklyViewDelegate,
          onControllerReadyCallback: (controller) => {
            this.kuiklyController = controller;
          },
          nativeManager: globalNativeManager,
        });
      }
    }
    .width('100%')
    .height('100%')
    .expandSafeArea([SafeAreaType.KEYBOARD]);
  }
}
