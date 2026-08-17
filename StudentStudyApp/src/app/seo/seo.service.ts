import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { Meta, Title } from '@angular/platform-browser';
import { ActivatedRouteSnapshot, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { DEFAULT_SEO, SEO_SITE } from './seo.constants';

export interface SeoData {
  title?: string;
  description?: string;
  keywords?: string;
  image?: string;
  canonical?: string;
  type?: string;
  noIndex?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SeoService {
  private readonly jsonLdId = 'voidmain-jsonld';

  constructor(
    private router: Router,
    private title: Title,
    private meta: Meta,
    @Inject(DOCUMENT) private document: Document
  ) { }

  watchRouteChanges(): void {
    this.applyRouteSeo();
    this.router.events
      .pipe(filter(event => event instanceof NavigationEnd))
      .subscribe(() => this.applyRouteSeo());
  }

  setPageSeo(data: SeoData, schema?: object | object[]): void {
    const seo = this.normalizeSeo(data);
    this.writeSeo(seo, schema);
  }

  setCourseSeo(course: {
    id: string;
    title: string;
    description: string;
    difficulty?: string;
    language?: string;
    duration?: number;
    price?: number;
    thumbnailUrl?: string;
    trainerName?: string;
  }, image?: string): void {
    const courseImage = this.absoluteUrl(image || course.thumbnailUrl || SEO_SITE.image);
    const title = `${course.title} Course in Hyderabad | ${SEO_SITE.name}`;
    const description = this.trimDescription(course.description || `Learn ${course.title} with practical training, mentor support, and project guidance at ${SEO_SITE.name}.`);

    this.writeSeo({
      title,
      description,
      keywords: `${course.title}, ${course.title} course Hyderabad, ${course.title} training Dilsukhnagar, ${DEFAULT_SEO.keywords}`,
      image: courseImage,
      canonical: `${SEO_SITE.url}/courses/preview/${course.id}`,
      type: 'article',
      noIndex: false
    }, [
      this.organizationSchema(),
      this.websiteSchema(),
      {
        '@context': 'https://schema.org',
        '@type': 'Course',
        '@id': `${SEO_SITE.url}/courses/preview/${course.id}#course`,
        name: course.title,
        description,
        image: courseImage,
        provider: {
          '@type': 'EducationalOrganization',
          name: SEO_SITE.name,
          sameAs: SEO_SITE.url
        },
        educationalLevel: course.difficulty || 'Beginner to Advanced',
        inLanguage: course.language || 'English',
        timeRequired: course.duration ? `PT${course.duration}H` : undefined,
        offers: {
          '@type': 'Offer',
          price: course.price || 0,
          priceCurrency: 'INR',
          availability: 'https://schema.org/InStock',
          url: `${SEO_SITE.url}/courses/preview/${course.id}`
        },
        instructor: course.trainerName ? {
          '@type': 'Person',
          name: course.trainerName
        } : undefined
      },
      this.breadcrumbSchema([
        { name: 'Home', url: SEO_SITE.url },
        { name: 'Courses', url: `${SEO_SITE.url}/courses` },
        { name: course.title, url: `${SEO_SITE.url}/courses/preview/${course.id}` }
      ])
    ]);
  }

  private applyRouteSeo(): void {
    const snapshot = this.deepestChild(this.router.routerState.snapshot.root);
    const data = (snapshot.data?.['seo'] || {}) as SeoData;
    this.writeSeo(this.normalizeSeo(data), this.schemaForCurrentPage(data));
  }

  private normalizeSeo(data: SeoData): Required<SeoData> {
    const canonicalPath = data.canonical || this.router.url.split('?')[0].split('#')[0];
    return {
      title: data.title || DEFAULT_SEO.title,
      description: this.trimDescription(data.description || DEFAULT_SEO.description),
      keywords: data.keywords || DEFAULT_SEO.keywords,
      image: this.absoluteUrl(data.image || DEFAULT_SEO.image),
      canonical: this.absoluteUrl(canonicalPath === '/home' ? '/' : canonicalPath),
      type: data.type || DEFAULT_SEO.type,
      noIndex: Boolean(data.noIndex)
    };
  }

  private writeSeo(seo: Required<SeoData>, schema?: object | object[]): void {
    this.title.setTitle(seo.title);
    this.meta.updateTag({ name: 'description', content: seo.description });
    this.meta.updateTag({ name: 'keywords', content: seo.keywords });
    this.meta.updateTag({ name: 'author', content: SEO_SITE.name });
    this.meta.updateTag({ name: 'publisher', content: SEO_SITE.name });
    this.meta.updateTag({ name: 'robots', content: seo.noIndex ? 'noindex, nofollow' : 'index, follow, max-image-preview:large, max-snippet:-1, max-video-preview:-1' });
    this.meta.updateTag({ name: 'theme-color', content: '#07111c' });
    this.meta.updateTag({ name: 'geo.position', content: `${SEO_SITE.geo.latitude};${SEO_SITE.geo.longitude}` });
    this.meta.updateTag({ name: 'geo.placename', content: 'Dilsukhnagar, Hyderabad, Telangana' });
    this.meta.updateTag({ name: 'geo.region', content: 'IN-TG' });
    this.meta.updateTag({ name: 'ICBM', content: `${SEO_SITE.geo.latitude}, ${SEO_SITE.geo.longitude}` });

    this.meta.updateTag({ property: 'og:title', content: seo.title });
    this.meta.updateTag({ property: 'og:description', content: seo.description });
    this.meta.updateTag({ property: 'og:type', content: seo.type });
    this.meta.updateTag({ property: 'og:url', content: seo.canonical });
    this.meta.updateTag({ property: 'og:image', content: seo.image });
    this.meta.updateTag({ property: 'og:site_name', content: SEO_SITE.name });
    this.meta.updateTag({ property: 'og:locale', content: 'en_IN' });

    this.meta.updateTag({ name: 'twitter:card', content: 'summary_large_image' });
    this.meta.updateTag({ name: 'twitter:title', content: seo.title });
    this.meta.updateTag({ name: 'twitter:description', content: seo.description });
    this.meta.updateTag({ name: 'twitter:image', content: seo.image });

    this.setCanonical(seo.canonical);
    this.setJsonLd(schema || this.schemaForCurrentPage(seo));
  }

  private schemaForCurrentPage(data: SeoData): object[] {
    const canonical = this.absoluteUrl(data.canonical || this.router.url.split('?')[0].split('#')[0]);
    const title = data.title || DEFAULT_SEO.title;
    const description = data.description || DEFAULT_SEO.description;

    return [
      this.organizationSchema(),
      this.localBusinessSchema(),
      this.websiteSchema(),
      {
        '@context': 'https://schema.org',
        '@type': data.type === 'article' ? 'Article' : 'WebPage',
        '@id': `${canonical}#webpage`,
        url: canonical,
        name: title,
        description,
        isPartOf: {
          '@id': `${SEO_SITE.url}/#website`
        },
        primaryImageOfPage: {
          '@type': 'ImageObject',
          url: this.absoluteUrl(data.image || SEO_SITE.image)
        },
        about: [
          { '@type': 'Thing', name: 'Software Courses Training' },
          { '@type': 'Thing', name: 'Internships' },
          { '@type': 'Thing', name: 'Academic Projects' }
        ]
      },
      this.breadcrumbForUrl(canonical, title)
    ];
  }

  private organizationSchema(): object {
    return {
      '@context': 'https://schema.org',
      '@type': 'EducationalOrganization',
      '@id': `${SEO_SITE.url}/#organization`,
      name: SEO_SITE.name,
      legalName: SEO_SITE.legalName,
      url: SEO_SITE.url,
      logo: SEO_SITE.logo,
      image: SEO_SITE.image,
      email: SEO_SITE.email,
      telephone: SEO_SITE.primaryPhone,
      description: DEFAULT_SEO.description,
      address: this.postalAddressSchema(),
      geo: this.geoSchema(),
      contactPoint: [
        {
          '@type': 'ContactPoint',
          contactType: 'customer support',
          telephone: SEO_SITE.supportPhone,
          email: SEO_SITE.email,
          areaServed: 'IN',
          availableLanguage: ['English', 'Telugu', 'Hindi']
        }
      ]
    };
  }

  private localBusinessSchema(): object {
    return {
      '@context': 'https://schema.org',
      '@type': 'LocalBusiness',
      '@id': `${SEO_SITE.url}/#localbusiness`,
      name: SEO_SITE.name,
      url: SEO_SITE.url,
      image: SEO_SITE.image,
      telephone: SEO_SITE.primaryPhone,
      email: SEO_SITE.email,
      address: this.postalAddressSchema(),
      geo: this.geoSchema(),
      openingHoursSpecification: SEO_SITE.businessHours,
      priceRange: '$$'
    };
  }

  private websiteSchema(): object {
    return {
      '@context': 'https://schema.org',
      '@type': 'WebSite',
      '@id': `${SEO_SITE.url}/#website`,
      url: SEO_SITE.url,
      name: SEO_SITE.name,
      description: DEFAULT_SEO.description,
      publisher: {
        '@id': `${SEO_SITE.url}/#organization`
      },
      potentialAction: {
        '@type': 'SearchAction',
        target: `${SEO_SITE.url}/courses?search={search_term_string}`,
        'query-input': 'required name=search_term_string'
      }
    };
  }

  private breadcrumbForUrl(canonical: string, title: string): object {
    const url = new URL(canonical);
    const segments = url.pathname.split('/').filter(Boolean);
    const items = [{ name: 'Home', url: SEO_SITE.url }];
    let path = '';

    segments.forEach(segment => {
      path += `/${segment}`;
      items.push({
        name: segment.replace(/-/g, ' ').replace(/\b\w/g, letter => letter.toUpperCase()),
        url: `${SEO_SITE.url}${path}`
      });
    });

    if (items.length === 1) {
      items[0].name = title;
    }

    return this.breadcrumbSchema(items);
  }

  private breadcrumbSchema(items: Array<{ name: string; url: string }>): object {
    return {
      '@context': 'https://schema.org',
      '@type': 'BreadcrumbList',
      itemListElement: items.map((item, index) => ({
        '@type': 'ListItem',
        position: index + 1,
        name: item.name,
        item: item.url
      }))
    };
  }

  private postalAddressSchema(): object {
    return {
      '@type': 'PostalAddress',
      streetAddress: SEO_SITE.address.streetAddress,
      addressLocality: SEO_SITE.address.locality,
      addressRegion: SEO_SITE.address.region,
      postalCode: SEO_SITE.address.postalCode,
      addressCountry: SEO_SITE.address.country
    };
  }

  private geoSchema(): object {
    return {
      '@type': 'GeoCoordinates',
      latitude: SEO_SITE.geo.latitude,
      longitude: SEO_SITE.geo.longitude
    };
  }

  private setCanonical(url: string): void {
    let link = this.document.querySelector<HTMLLinkElement>('link[rel="canonical"]');
    if (!link) {
      link = this.document.createElement('link');
      link.setAttribute('rel', 'canonical');
      this.document.head.appendChild(link);
    }
    link.setAttribute('href', url);
  }

  private setJsonLd(schema: object | object[]): void {
    let script = this.document.getElementById(this.jsonLdId) as HTMLScriptElement | null;
    if (!script) {
      script = this.document.createElement('script');
      script.id = this.jsonLdId;
      script.type = 'application/ld+json';
      this.document.head.appendChild(script);
    }
    script.text = JSON.stringify(Array.isArray(schema) ? schema : [schema]);
  }

  private deepestChild(snapshot: ActivatedRouteSnapshot): ActivatedRouteSnapshot {
    let route = snapshot;
    while (route.firstChild) {
      route = route.firstChild;
    }
    return route;
  }

  private absoluteUrl(value: string): string {
    if (/^https?:\/\//i.test(value)) {
      return value;
    }
    return `${SEO_SITE.url}${value.startsWith('/') ? value : `/${value}`}`;
  }

  private trimDescription(value: string): string {
    return value.length > 160 ? `${value.slice(0, 157).trim()}...` : value;
  }
}
