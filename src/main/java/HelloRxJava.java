import io.reactivex.*;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class HelloRxJava {
    public static void main(String[] args) throws Exception {
        // Publisherであるflowable instanceを生成
        Flowable<String> flowable =
                Flowable.create(new FlowableOnSubscribe<String>() {
                    @Override
                    public void subscribe(FlowableEmitter<String> emitter) throws Exception {
                        String[] datas = { "Corda Certified", "RxJava", "Flowable" };
                        for (String data : datas) {
                            if (emitter.isCancelled()) {
                                return;
                            }
                            emitter.onNext(data);
                        }
                        emitter.onComplete();
                    }
                }, BackpressureStrategy.BUFFER);

        // Subscriberを生成し、それをPublisherのSubscriberに食わせる
        flowable.observeOn(Schedulers.computation())
                .subscribe(new Subscriber<String>() {

                    private Subscription subscription;

                    // Subscribeした時の処理
                    @Override
                    public void onSubscribe(Subscription s) {
                        // SubscriptionをSubscriber内で保持する
                        this.subscription = s;
                        // 受け取るデータ数のrequest
                        this.subscription.request(1L);
                    }

                    // Publisher(Flowable, Observable)から通知を受け取った時の処理
                    @Override
                    public void onNext(String data) {
                        // 現在のthreadと受け取るdataを表示する
                        String threadName = Thread.currentThread().getName();
                        System.out.println(String.format("%s: %s", threadName, data));

                        // 次のbackressure request
                        this.subscription.request(1L);

                    }

                    // Publisherからエラー通知を受け取った時の処理
                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();

                    }

                    // Publisherから完了通知を受け取った時の処理
                    @Override
                    public void onComplete() {
                        String threadName = Thread.currentThread().getName();
                        System.out.println(String.format("%s: 完了しました", threadName));

                    }
                });
        Thread.sleep(500L);
    }
}