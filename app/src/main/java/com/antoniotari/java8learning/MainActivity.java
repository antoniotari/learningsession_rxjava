package com.antoniotari.java8learning;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MainActivity extends ActionBarActivity {

    TextView mTextView;
    Observable<String> mMyObservable;
    MySubscriber mMySubscriber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text1);
//        mMyObservable = Observable.create(new Observable.OnSubscribe<String>() {
//            @Override
//            public void call(Subscriber<? super String> sub) {
//                sub.onNext("Hello, world!");
//                sub.onCompleted();
//            }
//        });


        mMyObservable = Observable.create(this::callOnSubscribe);
        mMySubscriber = new MySubscriber(mTextView::setText);
        mMyObservable.subscribe(mMySubscriber);

        //another way to do it, passing the "onNext" value directly with this utility method
        Observable<String> myObservable2 = Observable.just("Hello, world!");
        //then,without defining a class,decide what to do with the callbacks
        Action1<String> onNextAction = new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println(s);
            }
        };
        Action1<Throwable> onErrorAction = (System.out::println);
        Action0 onCompleteAction = (() -> System.out.println("onComplete"));

        //print Hello World
        myObservable2.subscribe(onNextAction, onErrorAction, onCompleteAction);

        //chain all together
        Observable.just("Hello, world! again!").subscribe(System.out::println, System.out::println, () -> {
        });

        //Wouldn't it be cool if I could transform "Hello, world!" with some intermediary step?
        //in this example we skip onError and onComplete
        Observable.just("Hello, world!")
                .map(s -> s + " - Antonio")
                .subscribe(System.out::println);

        //WITHOUT lambdas
//        Observable.just("Hello, world!")
//                .map(new Func1<String, String>() {
//                    @Override
//                    public String call(String s) {
//                        return s + " -Dan";
//                    }
//                })
//                .subscribe(s -> System.out.println(s));

        //Here's an interesting aspect of map(): it does not have to emit items of the same type as the source Observable!
        //Suppose my Subscriber is not interested in outputting the original text, but instead wants to output the hash of the text

        Observable.just("Hello, world!")
                .map(new Func1<String, Integer>() {
                    @Override
                    public Integer call(String s) {
                        return s.hashCode();
                    }
                })
                .subscribe(i -> System.out.println(Integer.toString(i)));

        //we want our Subscriber to do as little as possible. Let's throw in another map() to convert our hash back into a String:
        Observable.just("Hello, world!")
                .map(s -> s.hashCode())
                .map(i -> Integer.toString(i))
                .subscribe(s -> System.out.println(s));

        searchWeb();
        searchWebBetter();
    }

    public void callOnSubscribe(Subscriber sub) {
        sub.onNext("Rock On!");
        sub.onCompleted();
    }

    private void searchWeb() {
        query("Hello, world!")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(urls -> {
                    for (String url : urls) {
                        System.out.println(url);
                    }
                });
    }

    private void searchWebBetter() {
        query("Hello, world!")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                        //flatMap() can return any Observable it wants.
                        //loops the list and extracts the single element then passes it to subscrive so we don't have to loop
                .flatMap(new Func1<List<String>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<String> urls) {
                        return Observable.from(urls);
                    }
                })
                        //we can chain another flatMap() to do more operations on the list, this time I'm using lambda
                .flatMap(url -> getTheTitle(url))
                        //getTitle() returns null if the URL 404s. We don't want to output "null"; it turns out we can filter them out!
                        //filter() emits the same item it received, but only if it passes the boolean check.
                .filter(title -> title != null)
                        //And now we want to only show 5 results at most:
                .take(5)
                .doOnNext(new Action1<String>() {
                    @Override
                    public void call(final String s) {
                        //do something on next, ie. saving to disk
                    }
                })
                .subscribe(url -> System.out.println(url));
    }

    private Observable<String> getTheTitle(String url) {
        //return the operation on the url string
        return Observable.from("dummy");
    }

    private Observable<List<String>> query(String text) {
        return Observable.create(new Observable.OnSubscribe<List<String>>() {
            @Override
            public void call(Subscriber<? super List<String>> sub) {
                //simulate network call
                try {
                    Thread.sleep(4444);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //search the web
                List<String> result = new ArrayList<String>();
                result.add("antonio");
                result.add("tari");
                sub.onNext(result);
                //sub.onCompleted();

                //simulate another longer network call
                try {
                    Thread.sleep(5555);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //search the web
                List<String> result2 = new ArrayList<String>();
                result2.add("antonio2");
                result2.add("tari2");
                sub.onNext(result2);
                sub.onCompleted();
            }
        });
    }

    /**
     * Subscriber<T> is an abstract class tha implements Observer<T> and Subscription
     */
    private static class MySubscriber extends Subscriber<String> {

        SubscriberListener mSubscriberListener;

        public MySubscriber(SubscriberListener listener) {
            mSubscriberListener = listener;
        }

        @Override
        public void onNext(String s) {
            mSubscriberListener.onNext(s);
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
        }
    }

    /**
     *
     */
    interface SubscriberListener {
        void onNext(String s);
    }
}
