import {AsyncTestCompleter,
  beforeEach,
  ddescribe,
  describe,
  expect,
  iit,
  inject,
  it,
  xdescribe,
  xit} from 'angular2/test_lib';
import {bootstrap} from 'angular2/src/core/application';
import {appDocumentToken,
  appElementToken} from 'angular2/src/core/application_tokens';
import {Component,
  Directive} from 'angular2/src/core/annotations_impl/annotations';
import {DOM} from 'angular2/src/dom/dom_adapter';
import {ListWrapper} from 'angular2/src/facade/collection';
import {PromiseWrapper} from 'angular2/src/facade/async';
import {bind,
  Inject} from 'angular2/di';
import {View} from 'angular2/src/core/annotations_impl/view';
import {LifeCycle} from 'angular2/src/core/life_cycle/life_cycle';
import {Testability,
  TestabilityRegistry} from 'angular2/src/core/testability/testability';
class HelloRootCmp {
  constructor() {
    this.greeting = 'hello';
  }
}
Object.defineProperty(HelloRootCmp, "annotations", {get: function() {
    return [new Component({selector: 'hello-app'}), new View({template: '{{greeting}} world!'})];
  }});
class HelloRootCmpContent {
  constructor() {}
}
Object.defineProperty(HelloRootCmpContent, "annotations", {get: function() {
    return [new Component({selector: 'hello-app'}), new View({template: 'before: <content></content> after: done'})];
  }});
class HelloRootCmp2 {
  constructor() {
    this.greeting = 'hello';
  }
}
Object.defineProperty(HelloRootCmp2, "annotations", {get: function() {
    return [new Component({selector: 'hello-app-2'}), new View({template: '{{greeting}} world, again!'})];
  }});
class HelloRootCmp3 {
  constructor(appBinding) {
    this.appBinding = appBinding;
  }
}
Object.defineProperty(HelloRootCmp3, "annotations", {get: function() {
    return [new Component({selector: 'hello-app'}), new View({template: ''})];
  }});
Object.defineProperty(HelloRootCmp3, "parameters", {get: function() {
    return [[new Inject("appBinding")]];
  }});
class HelloRootCmp4 {
  constructor(lc) {
    this.lc = lc;
  }
}
Object.defineProperty(HelloRootCmp4, "annotations", {get: function() {
    return [new Component({selector: 'hello-app'}), new View({template: ''})];
  }});
Object.defineProperty(HelloRootCmp4, "parameters", {get: function() {
    return [[new Inject(LifeCycle)]];
  }});
class HelloRootMissingTemplate {}
Object.defineProperty(HelloRootMissingTemplate, "annotations", {get: function() {
    return [new Component({selector: 'hello-app'})];
  }});
class HelloRootDirectiveIsNotCmp {}
Object.defineProperty(HelloRootDirectiveIsNotCmp, "annotations", {get: function() {
    return [new Directive({selector: 'hello-app'})];
  }});
export function main() {
  var fakeDoc,
      el,
      el2,
      testBindings,
      lightDom;
  beforeEach(() => {
    fakeDoc = DOM.createHtmlDocument();
    el = DOM.createElement('hello-app', fakeDoc);
    el2 = DOM.createElement('hello-app-2', fakeDoc);
    lightDom = DOM.createElement('light-dom-el', fakeDoc);
    DOM.appendChild(fakeDoc.body, el);
    DOM.appendChild(fakeDoc.body, el2);
    DOM.appendChild(el, lightDom);
    DOM.setText(lightDom, 'loading');
    testBindings = [bind(appDocumentToken).toValue(fakeDoc)];
  });
  describe('bootstrap factory method', () => {
    it('should throw if bootstrapped Directive is not a Component', inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootDirectiveIsNotCmp, testBindings, (e, t) => {
        throw e;
      });
      PromiseWrapper.then(refPromise, null, (reason) => {
        expect(reason.message).toContain(`Could not load 'HelloRootDirectiveIsNotCmp' because it is not a component.`);
        async.done();
      });
    }));
    it('should throw if no element is found', inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootCmp, [], (e, t) => {
        throw e;
      });
      PromiseWrapper.then(refPromise, null, (reason) => {
        expect(reason.message).toContain('The app selector "hello-app" did not match any elements');
        async.done();
      });
    }));
    it('should create an injector promise', () => {
      var refPromise = bootstrap(HelloRootCmp, testBindings);
      expect(refPromise).not.toBe(null);
    });
    it('should resolve an injector promise and contain bindings', inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootCmp, testBindings);
      refPromise.then((ref) => {
        expect(ref.injector.get(appElementToken)).toBe(el);
        async.done();
      });
    }));
    it('should provide the application component in the injector', inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootCmp, testBindings);
      refPromise.then((ref) => {
        expect(ref.injector.get(HelloRootCmp)).toBeAnInstanceOf(HelloRootCmp);
        async.done();
      });
    }));
    it('should display hello world', inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootCmp, testBindings);
      refPromise.then((ref) => {
        expect(ref.injector.get(appElementToken)).toHaveText('hello world!');
        async.done();
      });
    }));
    it('should support multiple calls to bootstrap', inject([AsyncTestCompleter], (async) => {
      var refPromise1 = bootstrap(HelloRootCmp, testBindings);
      var refPromise2 = bootstrap(HelloRootCmp2, testBindings);
      PromiseWrapper.all([refPromise1, refPromise2]).then((refs) => {
        expect(refs[0].injector.get(appElementToken)).toHaveText('hello world!');
        expect(refs[1].injector.get(appElementToken)).toHaveText('hello world, again!');
        async.done();
      });
    }));
    it("should make the provided bindings available to the application component", inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootCmp3, [testBindings, bind("appBinding").toValue("BoundValue")]);
      refPromise.then((ref) => {
        expect(ref.injector.get(HelloRootCmp3).appBinding).toEqual("BoundValue");
        async.done();
      });
    }));
    it("should avoid cyclic dependencies when root component requires Lifecycle through DI", inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootCmp4, testBindings);
      refPromise.then((ref) => {
        expect(ref.injector.get(HelloRootCmp4).lc).toBe(ref.injector.get(LifeCycle));
        async.done();
      });
    }));
    it("should support shadow dom content tag", inject([AsyncTestCompleter], (async) => {
      var refPromise = bootstrap(HelloRootCmpContent, testBindings);
      refPromise.then((ref) => {
        expect(ref.injector.get(appElementToken)).toHaveText('before: loading after: done');
        async.done();
      });
    }));
    it('should register each application with the testability registry', inject([AsyncTestCompleter], (async) => {
      var refPromise1 = bootstrap(HelloRootCmp, testBindings);
      var refPromise2 = bootstrap(HelloRootCmp2, testBindings);
      PromiseWrapper.all([refPromise1, refPromise2]).then((refs) => {
        var registry = refs[0].injector.get(TestabilityRegistry);
        PromiseWrapper.all([refs[0].injector.asyncGet(Testability), refs[1].injector.asyncGet(Testability)]).then((testabilities) => {
          expect(registry.findTestabilityInTree(el)).toEqual(testabilities[0]);
          expect(registry.findTestabilityInTree(el2)).toEqual(testabilities[1]);
          async.done();
        });
      });
    }));
  });
}
//# sourceMappingURL=application_spec.js.map

//# sourceMappingURL=./application_spec.map