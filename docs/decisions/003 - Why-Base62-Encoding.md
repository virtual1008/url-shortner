# Why Base62 Encoding Was Chosen

# Context

LinkScale is designed as a scalable URL infrastructure platform where every shortened URL should be:

* Compact
* URL-safe
* Human-readable
* Efficient for storage and sharing

After generating a unique Snowflake ID, the system converts that numeric value into a shorter string representation.

Example:

```text
Snowflake ID → Encoded Short Code
187654321987654 → xY7abQ
```

A critical architectural decision was selecting the encoding strategy for generating short URLs.

---

# System Requirements

The encoding mechanism should support:

* Very short URL generation
* URL-safe characters
* Human-readable strings
* Efficient encoding and decoding
* Good scalability
* Better user experience

---

# Alternatives Considered

# Option 1: Decimal Representation

Example:

```text
187654321987654
```

---

## Advantages

### Simple Representation

Easy to generate and understand.

---

### No Encoding Logic Needed

The system can directly expose the numeric ID.

---

## Problems

### Very Long URLs

Numeric IDs grow rapidly.

Example:

```text
https://linkscale.io/187654321987654
```

This creates:

* Poor readability
* Longer URLs
* Worse sharing experience

---

### Poor User Experience

Large numbers are difficult to:

* Remember
* Type manually
* Share verbally

---

# Option 2: Hexadecimal Encoding (Base16)

Example:

```text
AA12FF09
```

---

## Advantages

### Compact Compared to Decimal

Hex reduces string length compared to raw numbers.

---

### Easy Conversion

Widely supported encoding format.

---

## Problems

### Not Compact Enough

Base16 only uses:

```text
0-9 and A-F
```

This limits the compression efficiency.

The generated URLs are still longer than necessary.

---

### Less Human Friendly

Hexadecimal strings can become visually repetitive.

Example:

```text
A1FF90BC
```

---

# Option 3: Base64 Encoding

Example:

```text
ab+/XZ==
```

---

## Advantages

### Very Compact

Base64 provides better compression efficiency.

---

### Widely Used

Commonly used in data transmission systems.

---

## Problems

### URL Unsafe Characters

Base64 includes characters like:

```text
+
/
=
```

These characters can create issues in URLs.

This may require:

* URL encoding
* Additional escaping logic

---

### Less Clean URLs

Special characters reduce readability.

Example:

```text
https://linkscale.io/a+/=
```

---

# Option 4: Base62 Encoding (Chosen)

LinkScale uses Base62 encoding for generating short URL codes.

---

# What Is Base62 Encoding

Base62 uses:

```text
0-9
a-z
A-Z
```

Total characters:

```text
62 characters
```

This allows large numeric values to be represented using shorter strings.

---

# Example Conversion

```text
187654321987654 → xY7abQ
```

The large numeric Snowflake ID becomes a compact URL-safe short code.

---

# Why Base62 Was Chosen

## Compact URL Generation

Base62 significantly reduces URL length.

Example:

```text
Decimal:
187654321987654

Base62:
xY7abQ
```

This creates:

* Cleaner URLs
* Shorter links
* Better usability

---

## URL Safe Characters

Base62 avoids problematic symbols.

Only alphanumeric characters are used:

```text
0-9 a-z A-Z
```

This means:

* No URL escaping
* No special character issues
* Better browser compatibility

---

## Better User Experience

Shorter links are easier to:

* Read
* Share
* Copy
* Type manually

Example:

```text
https://linkscale.io/xY7abQ
```

---

## Efficient Storage

Shorter encoded strings reduce:

* Storage size
* Index size
* Memory usage

especially at large scale.

---

## Faster Lookups

Compact indexed strings improve:

* Database indexing
* Query performance
* Cache efficiency

---

## Perfect Pairing With Snowflake IDs

Snowflake generates compact numeric IDs.

Base62 efficiently transforms them into human-friendly short codes.

Architecture flow:

```text
Snowflake ID → Base62 Encoding → Short URL
```

Example:

```text
785412369874 → aZ91Kx
```

---

# Why Not Random String Generation

Another approach could be generating random characters directly.

Example:

```text
xA91kP
```

---

## Problems With Random Generation

### Collision Risk

Random generation can produce duplicate values.

This requires:

* Collision checks
* Retry logic
* Additional database queries

---

### Harder Scalability

As traffic grows:

* Collision probability increases
* Coordination becomes harder

---

### Poor Predictability for System Internals

Random values lose useful ordering properties.

---

# Why Base62 + Snowflake Is Better

Using:

```text
Snowflake ID + Base62 Encoding
```

provides:

* Guaranteed uniqueness
* Compact representation
* High scalability
* Ordered IDs internally
* Human-friendly URLs externally

without collision handling complexity.

---

# Scalability Benefits

Base62 supports future scaling requirements such as:

* Billions of URLs
* Distributed systems
* Multi-node deployments
* Analytics systems
* Cache optimization

without changing the URL format.

---

# Tradeoff Summary

| Decimal       | Hexadecimal    | Base64        | Base62 (Chosen) |
| ------------- | -------------- | ------------- | --------------- |
| Very long     | Medium length  | Compact       | Compact         |
| URL-safe      | URL-safe       | Special chars | URL-safe        |
| Easy          | Easy           | Moderate      | Moderate        |
| Poor UX       | Average UX     | Symbol issues | Best UX         |
| Large storage | Medium storage | Efficient     | Efficient       |

---

# Current Architectural Philosophy

The project prioritizes:

```text
Scalable infrastructure with clean and user-friendly URL generation.
```

Base62 encoding provides a strong balance between:

* Compactness
* Performance
* Simplicity
* Scalability
* User experience

---

# Final Decision

Base62 encoding was selected because it provides:

* Short URL generation
* URL-safe encoding
* Better readability
* Efficient storage
* Excellent compatibility with Snowflake IDs
* Scalable architecture support

while maintaining simplicity and high performance for a production-oriented URL shortening platform.
